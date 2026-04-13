package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.dto.report.IndicatorReportData;
import com.abetappteam.abetapp.dto.report.MultiYearReportData;
import com.abetappteam.abetapp.dto.report.OutcomeReportData;
import com.abetappteam.abetapp.dto.report.ReportMeasureData;
import com.abetappteam.abetapp.entity.Course;
import com.abetappteam.abetapp.entity.CourseIndicator;
import com.abetappteam.abetapp.entity.Measure;
import com.abetappteam.abetapp.entity.MeasureResult;
import com.abetappteam.abetapp.entity.Outcome;
import com.abetappteam.abetapp.entity.PerformanceIndicator;
import com.abetappteam.abetapp.entity.Semester;
import com.abetappteam.abetapp.exception.BusinessException;
import com.abetappteam.abetapp.repository.CourseIndicatorRepository;
import com.abetappteam.abetapp.repository.CourseRepository;
import com.abetappteam.abetapp.repository.MeasureRepository;
import com.abetappteam.abetapp.repository.MeasureResultRepository;
import com.abetappteam.abetapp.repository.OutcomeRepository;
import com.abetappteam.abetapp.repository.PerformanceIndicatorRepository;
import com.abetappteam.abetapp.repository.SemesterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for generating multi-year summary reports.
 */
@Service
public class MultiYearReportService {

    private static final Logger logger = LoggerFactory.getLogger(MultiYearReportService.class);

    private final SemesterRepository semesterRepository;
    private final CourseRepository courseRepository;
    private final CourseIndicatorRepository courseIndicatorRepository;
    private final MeasureRepository measureRepository;
    private final MeasureResultRepository measureResultRepository;
    private final OutcomeRepository outcomeRepository;
    private final PerformanceIndicatorRepository performanceIndicatorRepository;

    @Autowired
    public MultiYearReportService(
            SemesterRepository semesterRepository,
            CourseRepository courseRepository,
            CourseIndicatorRepository courseIndicatorRepository,
            MeasureRepository measureRepository,
            MeasureResultRepository measureResultRepository,
            OutcomeRepository outcomeRepository,
            PerformanceIndicatorRepository performanceIndicatorRepository) {
        this.semesterRepository = semesterRepository;
        this.courseRepository = courseRepository;
        this.courseIndicatorRepository = courseIndicatorRepository;
        this.measureRepository = measureRepository;
        this.measureResultRepository = measureResultRepository;
        this.outcomeRepository = outcomeRepository;
        this.performanceIndicatorRepository = performanceIndicatorRepository;
    }

    // Returns the semesters for the given program whose dates fall within
    // startDate, endDate
    @Transactional(readOnly = true)
    public List<Semester> getSemestersInDateRange(Long programId, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("End date cannot be before start date");
        }
        return semesterRepository.searchSemesters(null, null, null, startDate, endDate, null, null, null)
                .stream()
                .filter(s -> programId.equals(s.getProgramId()))
                .toList();
    }

    // Returns all active measures for the given program across the date range
    @Transactional(readOnly = true)
    public List<Measure> getMeasuresInDateRange(Long programId, LocalDate startDate, LocalDate endDate) {
        // Validate date range via getSemestersInDateRange
        List<Semester> semesters = getSemestersInDateRange(programId, startDate, endDate);
        logger.info("Found {} semesters in date range [{}, {}] for program {}",
                semesters.size(), startDate, endDate, programId);

        List<Course> courses = courseRepository.findActiveCoursesByProgramId(programId);
        List<Measure> output = new ArrayList<>();

        for (Semester semester : semesters) {
            for (Course course : courses) {
                List<CourseIndicator> indicators = courseIndicatorRepository.findByCourseIdAndIsActive(course.getId(),
                        true);

                for (CourseIndicator ci : indicators) {
                    output.addAll(measureRepository.searchMeasures(
                            null, ci.getId().intValue(), semester.getId().intValue(), true));
                }
            }
        }

        logger.info("Aggregated {} measures for program {} in date range [{}, {}]",
                output.size(), programId, startDate, endDate);
        return output;
    }

    /**
     * Builds a multi-year report
     */
    @Transactional(readOnly = true)
    public MultiYearReportData buildHierarchicalReport(Long programId, LocalDate startDate, LocalDate endDate) {
        // Validate date range
        List<Semester> semesters = getSemestersInDateRange(programId, startDate, endDate);
        if (semesters.isEmpty()) {
            throw new BusinessException("No semesters found in the selected date range");
        }

        logger.info("Building hierarchical report for program {} from {} to {}", programId, startDate, endDate);

        // Get distinct outcomes from all semesters in range
        Set<Outcome> outcomeSet = new HashSet<>();
        for (Semester semester : semesters) {
            List<Outcome> outcomes = outcomeRepository.findBySemesterIdAndActive(semester.getId(), true);
            outcomeSet.addAll(outcomes);
        }

        if (outcomeSet.isEmpty()) {
            throw new BusinessException("No student outcomes found for this program in the selected date range");
        }

        logger.info("Found {} distinct outcomes for program {}", outcomeSet.size(), programId);

        // Get all active courses for the program
        List<Course> courses = courseRepository.findActiveCoursesByProgramId(programId);

        // Build hierarchical structure
        List<OutcomeReportData> reportOutcomes = new ArrayList<>();

        for (Outcome outcome : outcomeSet) {
            // Get active performance indicators for this outcome
            List<PerformanceIndicator> indicators = performanceIndicatorRepository
                    .findByStudentOutcomeIdAndIsActive(outcome.getId(), true);

            if (indicators.isEmpty()) {
                continue;
            }

            List<IndicatorReportData> reportIndicators = new ArrayList<>();
            Set<String> recommendedActions = new HashSet<>();

            for (PerformanceIndicator indicator : indicators) {
                // Get course-indicator mappings for this performance indicator
                List<CourseIndicator> courseIndicators = courseIndicatorRepository
                        .findByIndicatorIdAndIsActive(indicator.getId(), true);

                for (CourseIndicator courseIndicator : courseIndicators) {
                    // Find the corresponding course
                    Course course = courses.stream()
                            .filter(c -> c.getId().equals(courseIndicator.getCourseId()))
                            .findFirst()
                            .orElse(null);

                    if (course == null) {
                        continue;
                    }

                    // Get measures for this course-indicator in the date range
                    List<Measure> measures = new ArrayList<>();
                    for (Semester semester : semesters) {
                        measures.addAll(measureRepository.searchMeasures(
                                null, courseIndicator.getId().intValue(),
                                semester.getId().intValue(), true));
                    }

                    if (!measures.isEmpty()) {
                        // Create report indicator
                        String indicatorNumber = String.format("%d.%d", outcome.getNumber(),
                                indicator.getIndicatorNumber());
                        IndicatorReportData reportIndicator = new IndicatorReportData(
                                indicator.getId(), indicatorNumber, course.getCourseCode(),
                                course.getStudentCount());

                        // Convert measures to report data
                        for (Measure measure : measures) {
                            // Get measure result for this measure (if exists)
                            List<MeasureResult> results = measureResultRepository.findByMeasureId(measure.getId());
                            MeasureResult result = results.isEmpty() ? null : results.get(0);

                            Double metPercentage = 0.0;
                            Integer met = 0;
                            Integer exceeded = 0;
                            Integer below = 0;
                            String status = "Not met";
                            String note = null;

                            if (result != null) {
                                met = result.getStudentsMet() != null ? result.getStudentsMet() : 0;
                                exceeded = result.getStudentsExceeded() != null ? result.getStudentsExceeded() : 0;
                                below = result.getStudentsBelow() != null ? result.getStudentsBelow() : 0;
                                metPercentage = calculateMetPercentage(met, exceeded, below);
                                status = determineStatus(metPercentage);
                                note = result.getObservation();
                            }

                            ReportMeasureData reportMeasure = new ReportMeasureData(
                                    measure.getId(), courseIndicator.getId(), course.getCourseCode(),
                                    measure.getDescription(),
                                    met, exceeded, below, metPercentage, status, note, measure.getRecommendedAction());

                            reportIndicator.addMeasure(reportMeasure);

                            // Collect recommended actions
                            if (measure.getRecommendedAction() != null
                                    && !measure.getRecommendedAction().trim().isEmpty()) {
                                recommendedActions.add(measure.getRecommendedAction());
                            }
                        }

                        reportIndicators.add(reportIndicator);
                    }
                }
            }

            if (!reportIndicators.isEmpty()) {
                // Determine outcome status from indicators
                String overallStatus = determineOutcomeStatus(reportIndicators);

                OutcomeReportData reportOutcome = new OutcomeReportData(
                        outcome.getNumber(), outcome.getDescription(), overallStatus);
                reportOutcome.setIndicators(reportIndicators);
                reportOutcome.setRecommendedActions(new ArrayList<>(recommendedActions));

                reportOutcomes.add(reportOutcome);
            }
        }

        if (reportOutcomes.isEmpty()) {
            throw new BusinessException("No measures found for the selected date range");
        }

        // Create response
        String semesterName = String.format("%s – %s", startDate, endDate);
        String academicYear = String.format("%s–%s", startDate.getYear(), endDate.getYear());
        String generatedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("M/d/yyyy"));

        MultiYearReportData response = new MultiYearReportData(
                semesters.get(0).getId(), semesterName, academicYear, generatedDate);
        response.setOutcomes(reportOutcomes);

        logger.info("Built hierarchical report with {} outcomes", reportOutcomes.size());
        return response;
    }

    /**
     * Calculates the percentage of students that met or exceeded the measure.
     */
    private Double calculateMetPercentage(Integer met, Integer exceeded, Integer below) {
        Integer total = met + exceeded + below;
        if (total == 0) {
            return 0.0;
        }
        return Math.round(((met + exceeded) / (double) total) * 1000) / 10.0;
    }

    /**
     * Determines the status string based on met percentage.
     */
    private String determineStatus(Double percentage) {
        if (percentage >= 80) {
            return "Met comfortably";
        } else if (percentage >= 70) {
            return "Met";
        } else if (percentage >= 65) {
            return "Barely not met";
        }
        return "Not met";
    }

    /**
     * Determines the overall outcome status based on its indicators' measures.
     */
    private String determineOutcomeStatus(List<IndicatorReportData> indicators) {
        if (indicators.isEmpty()) {
            return "No Data";
        }

        int totalMeasures = 0;
        int metMeasures = 0;

        for (IndicatorReportData indicator : indicators) {
            for (ReportMeasureData measure : indicator.getMeasures()) {
                totalMeasures++;
                if ("Met comfortably".equals(measure.getStatus()) || "Met".equals(measure.getStatus())) {
                    metMeasures++;
                }
            }
        }

        if (totalMeasures == 0) {
            return "No Data";
        }

        double percentage = (metMeasures / (double) totalMeasures) * 100;
        if (percentage >= 80) {
            return "MET";
        } else if (percentage >= 50) {
            return "Partially Met";
        }
        return "Not Met";
    }
}
