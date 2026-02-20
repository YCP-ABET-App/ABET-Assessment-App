package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.dto.importer.*;
import com.abetappteam.abetapp.dto.OutcomeDTO;
import com.abetappteam.abetapp.dto.PerformanceIndicatorDTO;
import com.abetappteam.abetapp.dto.CourseDTO;
import com.abetappteam.abetapp.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ImporterService {

    @Autowired private SemesterService semesterService;
    @Autowired private OutcomeService outcomeService;
    @Autowired private PerformanceIndicatorService indicatorService;
    @Autowired private CourseService courseService;
    @Autowired private CourseIndicatorService courseIndicatorService;
    @Autowired private MeasureService measureService;

    @Transactional
    public void importSummary(SummaryImportDTO dto) {

        Semester semester = semesterService.findById(dto.getSemesterId());

        for (OutcomeImportDTO o : dto.getOutcomes()) {

            // ========== OUTCOME ==========
            Outcome outcome = getOrCreateOutcome(semester.getId(), o);

            for (IndicatorImportDTO ind : o.getIndicators()) {

                // ind.getNumber() returns a double like 1.1, 2.3, etc.
                // Extract the indicator number (the part after the decimal)
                int indicatorNumber = extractIndicatorNumber(ind.getNumber());

                PerformanceIndicator pi =
                        getOrCreateIndicator(outcome.getId(), indicatorNumber);

                for (CourseImportDTO c : ind.getCourses()) {

                    // ========== COURSE ==========
                    Course course = getOrCreateCourse(
                            c.getCourseCode().trim().toUpperCase(),
                            semester.getId()
                    );

                    // ========== COURSE INDICATOR ==========
                    CourseIndicator ci =
                            courseIndicatorService.getOrCreate(course.getId(), pi.getId());

                    // ========== MEASURES ==========
                    for (MeasureImportDTO m : c.getMeasures()) {

                        Measure measure = new Measure();

                        measure.setCourseIndicatorId(ci.getId());

                        measure.setActive(true);
                        measure.setStatus(convertStatus(m.getStatus()));

                        measure.setDescription(m.getDescription());


                        // Join recommended actions array into single string
                        measure.setRecommendedAction(
                                m.getRecommendedActions() != null && !m.getRecommendedActions().isEmpty()
                                        ? String.join("\n", m.getRecommendedActions())
                                        : null
                        );

                        measure.setFcar(null);

                        // Calculate student counts from percentage
                        // Assume 30 students as default
                        int totalStudents = 30;
                        double percentage = m.getMetPercentage();

                        int metAndExceeded = (int) Math.round((percentage / 100.0) * totalStudents);
                        int below = totalStudents - metAndExceeded;

                        // Split met and exceeded based on status
                        int met = 0;
                        int exceeded = 0;

                        String status = m.getStatus() != null ? m.getStatus().toLowerCase() : "";

                        if (status.contains("comfortably")) {
                            // Met comfortably: ~40% exceeded
                            exceeded = (int) Math.round(metAndExceeded * 0.4);
                            met = metAndExceeded - exceeded;
                        } else if (status.contains("met") && !status.contains("not")) {
                            // Met: ~20% exceeded
                            exceeded = (int) Math.round(metAndExceeded * 0.2);
                            met = metAndExceeded - exceeded;
                        } else {
                            // Not met or barely not met: all in met category
                            met = metAndExceeded;
                            exceeded = 0;
                        }


                        measureService.createFromImport(measure);

                    }
                }
            }
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    /**
     * Extract indicator number from double like 1.1 -> 1, 2.3 -> 3, 6.5 -> 5
     */
    private int extractIndicatorNumber(double fullNumber) {
        // Get decimal part: 1.1 -> 0.1, multiply by 10 and round
        double fractionalPart = fullNumber - Math.floor(fullNumber);
        return (int) Math.round(fractionalPart * 10);
    }

    /**
     * Find or create an outcome for the given semester and outcome number.
     * First tries to find an existing active outcome by number and semester.
     */
    private Outcome getOrCreateOutcome(Long semesterId, OutcomeImportDTO o) {
        // Try to find existing outcome by semester and outcome number
        List<Outcome> allOutcomes = outcomeService.findActiveOutcomesBySemester(semesterId);

        for (Outcome existing : allOutcomes) {
            if (existing.getNumber() != null && existing.getNumber() == o.getNumber()) {
                return existing;
            }
        }

        // If not found, create new outcome
        OutcomeDTO dto = new OutcomeDTO();
        dto.setNumber(o.getNumber());
        dto.setDescription("Imported outcome " + o.getNumber());
        dto.setEvaluation(o.getStatus() != null ? o.getStatus() : "Pending");
        dto.setSemesterId(semesterId);
        dto.setActive(true);

        return outcomeService.create(dto);
    }

    /**
     * Find or create a performance indicator for the given outcome.
     * Matches by indicator number within the outcome.
     */
    private PerformanceIndicator getOrCreateIndicator(Long outcomeId, int indicatorNumber) {
        // Get all indicators for this outcome
        List<PerformanceIndicator> indicators = indicatorService.getIndicatorsByStudentOutcome(outcomeId);

        // Try to find one with matching indicator number
        for (PerformanceIndicator pi : indicators) {
            if (pi.getIndicatorNumber() != null && pi.getIndicatorNumber() == indicatorNumber) {
                return pi;
            }
        }

        // If not found, create new indicator
        PerformanceIndicatorDTO dto = new PerformanceIndicatorDTO();
        dto.setIndicatorNumber(indicatorNumber);
        dto.setDescription("Imported indicator " + indicatorNumber);
        dto.setStudentOutcomeId(outcomeId);
        dto.setIsActive(true);

        return indicatorService.createPerformanceIndicator(dto);
    }

    /**
     * Find or create a course for the given semester.
     * Matches by course code (case-insensitive).
     */
    private Course getOrCreateCourse(String code, Long semesterId) {
        // Try to find an existing course in this semester
        Course course = courseService.findByCourseCode(code);

        if (course.getCourseCode() != null &&
                course.getCourseCode().equalsIgnoreCase(code)) {
            return course;
        }

        // Create new course
        CourseDTO dto = new CourseDTO();
        dto.setCourseCode(code);
        dto.setCourseName(code);
        dto.setCourseDescription("Imported course " + code);
        //dto.setSemesterId(semesterId);
        dto.setStudentCount(0);
        dto.setIsActive(true);

        return courseService.createCourse(dto);
    }

    /**
     * Convert import status to internal measure status.
     */
    private String convertStatus(String status) {
        if (status == null) {
            return "InProgress";
        }

        return switch (status.toLowerCase()) {
            case "met", "met comfortably" -> "Complete";
            case "not met", "barely not met" -> "InReview";
            default -> "InProgress";
        };
    }
}