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
import com.abetappteam.abetapp.entity.ScheduleEntry;
import com.abetappteam.abetapp.entity.Section;
import com.abetappteam.abetapp.entity.SectionProgram;
import com.abetappteam.abetapp.entity.Semester;
import com.abetappteam.abetapp.exception.BusinessException;
import com.abetappteam.abetapp.repository.CourseIndicatorRepository;
import com.abetappteam.abetapp.repository.CourseRepository;
import com.abetappteam.abetapp.repository.MeasureRepository;
import com.abetappteam.abetapp.repository.MeasureResultRepository;
import com.abetappteam.abetapp.repository.OutcomeRepository;
import com.abetappteam.abetapp.repository.PerformanceIndicatorRepository;
import com.abetappteam.abetapp.repository.ScheduleEntryRepository;
import com.abetappteam.abetapp.repository.SectionProgramRepository;
import com.abetappteam.abetapp.repository.SectionRepository;
import com.abetappteam.abetapp.repository.SemesterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
    private final SectionProgramRepository sectionProgramRepository;
    private final SectionRepository sectionRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;

    @Autowired
    public MultiYearReportService(
            SemesterRepository semesterRepository,
            CourseRepository courseRepository,
            CourseIndicatorRepository courseIndicatorRepository,
            MeasureRepository measureRepository,
            MeasureResultRepository measureResultRepository,
            OutcomeRepository outcomeRepository,
            PerformanceIndicatorRepository performanceIndicatorRepository,
            SectionProgramRepository sectionProgramRepository,
            SectionRepository sectionRepository,
            ScheduleEntryRepository scheduleEntryRepository) {
        this.semesterRepository = semesterRepository;
        this.courseRepository = courseRepository;
        this.courseIndicatorRepository = courseIndicatorRepository;
        this.measureRepository = measureRepository;
        this.measureResultRepository = measureResultRepository;
        this.outcomeRepository = outcomeRepository;
        this.performanceIndicatorRepository = performanceIndicatorRepository;
        this.sectionProgramRepository = sectionProgramRepository;
        this.sectionRepository = sectionRepository;
        this.scheduleEntryRepository = scheduleEntryRepository;
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
        List<Semester> semesters = getSemestersInDateRange(programId, startDate, endDate);
        logger.info("Found {} semesters in date range [{}, {}] for program {}",
                semesters.size(), startDate, endDate, programId);

        List<Integer> semesterIds = semesters.stream()
                .map(s -> s.getId().intValue())
                .toList();

        List<Measure> output = measureRepository.findMeasuresByProgramAndSemesters(
                programId.intValue(), semesterIds, true);

        logger.info("Aggregated {} measures for program {} in date range [{}, {}]",
                output.size(), programId, startDate, endDate);
        return output;
    }

    /**
     * Builds a report for a single semester by ID.
     */
    @Transactional(readOnly = true)
    public MultiYearReportData buildReportForSemester(Long programId, Long semesterId) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new BusinessException("Semester not found: " + semesterId));

        Set<Integer> semesterIdSet = Set.of(semesterId.intValue());
        List<Semester> semesters = List.of(semester);

        String academicYear = semester.getAcademicYear() != null
                ? String.format("%d\u2013%d", semester.getAcademicYear(), semester.getAcademicYear() + 1)
                : String.valueOf(semester.getStartDate().getYear());
        String generatedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("M/d/yyyy"));

        return buildReportForSemesters(programId, semesterIdSet, semesters, academicYear, semester.getName(), generatedDate);
    }

    /**
     * Builds one report per academic year for the given date range.
     * Years with no measure data are included with an empty outcomes list.
     */
    @Transactional(readOnly = true)
    public List<MultiYearReportData> buildReportByAcademicYear(Long programId, LocalDate startDate, LocalDate endDate) {
        List<Semester> allSemesters = getSemestersInDateRange(programId, startDate, endDate);
        if (allSemesters.isEmpty()) {
            throw new BusinessException("No semesters found in the selected date range");
        }

        // Group semesters by academic year (sorted ascending)
        Map<Integer, List<Semester>> semestersByYear = allSemesters.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getAcademicYear() != null ? s.getAcademicYear() : s.getStartDate().getYear(),
                        TreeMap::new,
                        Collectors.toList()));

        String generatedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("M/d/yyyy"));
        List<MultiYearReportData> result = new ArrayList<>();

        for (Map.Entry<Integer, List<Semester>> entry : semestersByYear.entrySet()) {
            Integer year = entry.getKey();
            List<Semester> yearSemesters = entry.getValue();

            Set<Integer> semesterIdSet = yearSemesters.stream()
                    .map(s -> s.getId().intValue())
                    .collect(Collectors.toSet());

            String academicYear = String.format("%d\u2013%d", year, year + 1);
            LocalDate yearStart = yearSemesters.stream()
                    .map(Semester::getStartDate)
                    .min(Comparator.naturalOrder())
                    .orElse(startDate);
            LocalDate yearEnd = yearSemesters.stream()
                    .map(Semester::getEndDate)
                    .max(Comparator.naturalOrder())
                    .orElse(endDate);
            String semesterName = String.format("%s \u2013 %s", yearStart, yearEnd);

            MultiYearReportData yearReport;
            try {
                yearReport = buildReportForSemesters(
                        programId, semesterIdSet, yearSemesters, academicYear, semesterName, generatedDate);
            } catch (BusinessException e) {
                // No data for this year — include an empty placeholder
                logger.info("No data for academic year {}: {}", academicYear, e.getMessage());
                yearReport = new MultiYearReportData(
                        yearSemesters.get(0).getId(), semesterName, academicYear, generatedDate);
                yearReport.setOutcomes(new ArrayList<>());
            }
            result.add(yearReport);
        }

        return result;
    }

    /**
     * Builds a multi-year report aggregated across the entire date range (no year grouping).
     */
    @Transactional(readOnly = true)
    public MultiYearReportData buildHierarchicalReport(Long programId, LocalDate startDate, LocalDate endDate) {
        List<Semester> semesters = getSemestersInDateRange(programId, startDate, endDate);
        if (semesters.isEmpty()) {
            throw new BusinessException("No semesters found in the selected date range");
        }

        Set<Integer> semesterIdSet = semesters.stream()
                .map(s -> s.getId().intValue())
                .collect(Collectors.toSet());

        String semesterName = String.format("%s \u2013 %s", startDate, endDate);
        String academicYear = String.format("%s\u2013%s", startDate.getYear(), endDate.getYear());
        String generatedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("M/d/yyyy"));

        return buildReportForSemesters(programId, semesterIdSet, semesters, academicYear, semesterName, generatedDate);
    }

    /**
     * Core report building logic for a given set of semesters.
     *
     * Traversal order: program → sectionProgram → section
     * → course → courseIndicator → measures
     * ALSO: schedule_entry → measures (via schedule_entry_id)
     * → indicator → outcome → measure_results (via measure_id)
     */
    private MultiYearReportData buildReportForSemesters(
            Long programId,
            Set<Integer> semesterIdSet,
            List<Semester> semesters,
            String academicYear,
            String semesterName,
            String generatedDate) {

        logger.info("Building report for academic year {} ({} semesters)", academicYear, semesters.size());

        // program → section_programs → sections in semester set → course IDs
        List<SectionProgram> sectionPrograms = sectionProgramRepository.findByProgramId(programId.intValue());
        Set<Long> courseIdsInRange = new HashSet<>();
        for (SectionProgram sp : sectionPrograms) {
            sectionRepository.findById((long) sp.getSectionId())
                    .filter(sec -> semesterIdSet.contains(sec.getSemesterId()))
                    .ifPresent(sec -> courseIdsInRange.add((long) sec.getCourseId()));
        }

        if (courseIdsInRange.isEmpty()) {
            throw new BusinessException("No courses found for this program in the selected date range");
        }

        logger.info("Found {} courses for academic year {} via section path", courseIdsInRange.size(), academicYear);

        // Maps for building the hierarchical output
        Map<Long, OutcomeReportData> outcomeMap = new LinkedHashMap<>();
        Map<String, IndicatorReportData> indicatorMap = new LinkedHashMap<>();
        Map<Long, Set<String>> recommendedActionsMap = new HashMap<>();
        Map<Long, String> outcomeEvaluationMap = new HashMap<>();
        Set<Long> processedMeasureIds = new HashSet<>();

        for (Long courseId : courseIdsInRange) {
            Course course = courseRepository.findById(courseId)
                    .filter(Course::getIsActive)
                    .orElse(null);
            if (course == null) continue;

            List<CourseIndicator> courseIndicators = courseIndicatorRepository.findByCourseIdAndIsActive(courseId, true);

            for (CourseIndicator ci : courseIndicators) {
                // Get measures via course_indicator_id (old data path)
                List<Measure> measures = measureRepository.findByCourseIndicatorId(ci.getId());

                // Also check schedule_entry for newer measures linked via schedule_entry_id
                List<ScheduleEntry> scheduleEntries = scheduleEntryRepository.findByCourseId(courseId.intValue())
                        .stream()
                        .filter(se -> semesterIdSet.contains(se.getSemesterId())
                                && se.getIndicatorId() == ci.getIndicatorId().intValue())
                        .toList();
                for (ScheduleEntry se : scheduleEntries) {
                    List<Measure> newMeasures = measureRepository.findByScheduleEntryId(se.getId());
                    for (Measure m : newMeasures) {
                        if (measures.stream().noneMatch(existing -> existing.getId().equals(m.getId()))) {
                            measures = new ArrayList<>(measures);
                            measures.add(m);
                        }
                    }
                }

                if (measures.isEmpty()) continue;

                PerformanceIndicator indicator = performanceIndicatorRepository.findById(ci.getIndicatorId())
                        .filter(pi -> Boolean.TRUE.equals(pi.getIsActive()))
                        .orElse(null);
                if (indicator == null) continue;

                Outcome outcome = outcomeRepository.findById(indicator.getStudentOutcomeId())
                        .filter(o -> Boolean.TRUE.equals(o.getActive()))
                        .orElse(null);
                if (outcome == null) continue;

                String indicatorKey = outcome.getId() + "_" + indicator.getId() + "_" + courseId;
                String indicatorNumber = String.format("%d.%d", outcome.getNumber(), indicator.getIndicatorNumber());
                IndicatorReportData reportIndicator = indicatorMap.computeIfAbsent(indicatorKey,
                        k -> new IndicatorReportData(indicator.getId(), indicatorNumber, course.getCourseCode(),
                                course.getStudentCount()));

                outcomeMap.computeIfAbsent(outcome.getId(),
                        k -> new OutcomeReportData(outcome.getId(), outcome.getNumber(), outcome.getDescription(), ""));
                recommendedActionsMap.computeIfAbsent(outcome.getId(), k -> new HashSet<>());
                outcomeEvaluationMap.putIfAbsent(outcome.getId(), outcome.getEvaluation());

                for (Measure measure : measures) {
                    if (!processedMeasureIds.add(measure.getId())) continue;

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
                            measure.getId(), ci.getId(), course.getCourseCode(),
                            measure.getDescription(), met, exceeded, below, metPercentage, status, note,
                            measure.getRecommendedAction());

                    reportIndicator.addMeasure(reportMeasure);

                    if (measure.getRecommendedAction() != null && !measure.getRecommendedAction().trim().isEmpty()) {
                        recommendedActionsMap.get(outcome.getId()).add(measure.getRecommendedAction());
                    }
                }
            }
        }

        if (outcomeMap.isEmpty()) {
            throw new BusinessException("No measures found for academic year " + academicYear);
        }

        // Assemble final outcome list with their indicators, sorted by outcome number
        List<OutcomeReportData> reportOutcomes = new ArrayList<>();
        List<Map.Entry<Long, OutcomeReportData>> sortedEntries = outcomeMap.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getValue().getOutcomeNumber()))
                .collect(Collectors.toList());

        for (Map.Entry<Long, OutcomeReportData> entry : sortedEntries) {
            Long outcomeId = entry.getKey();
            OutcomeReportData reportOutcome = entry.getValue();

            List<IndicatorReportData> indicators = indicatorMap.entrySet().stream()
                    .filter(e -> e.getKey().startsWith(outcomeId + "_"))
                    .map(Map.Entry::getValue)
                    .filter(ind -> !ind.getMeasures().isEmpty())
                    .collect(Collectors.toList());

            if (!indicators.isEmpty()) {
                String evaluation = outcomeEvaluationMap.get(outcomeId);
                String computedStatus = determineOutcomeStatus(indicators);
                reportOutcome.setOverallStatus(evaluation != null && !evaluation.isBlank() ? evaluation : computedStatus);
                reportOutcome.setIndicators(indicators);
                reportOutcome.setRecommendedActions(
                        new ArrayList<>(recommendedActionsMap.getOrDefault(outcomeId, new HashSet<>())));
                reportOutcomes.add(reportOutcome);
            }
        }

        if (reportOutcomes.isEmpty()) {
            throw new BusinessException("No measures found for academic year " + academicYear);
        }

        MultiYearReportData response = new MultiYearReportData(
                semesters.get(0).getId(), semesterName, academicYear, generatedDate);
        response.setOutcomes(reportOutcomes);

        logger.info("Built report for academic year {} with {} outcomes", academicYear, reportOutcomes.size());
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
