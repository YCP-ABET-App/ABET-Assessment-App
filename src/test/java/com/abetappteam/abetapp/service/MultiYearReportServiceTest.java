package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.BaseServiceTest;
import com.abetappteam.abetapp.dto.report.IndicatorReportData;
import com.abetappteam.abetapp.dto.report.MultiYearReportData;
import com.abetappteam.abetapp.dto.report.OutcomeReportData;
import com.abetappteam.abetapp.dto.report.ReportMeasureData;
import com.abetappteam.abetapp.entity.*;
import com.abetappteam.abetapp.exception.BusinessException;
import com.abetappteam.abetapp.repository.*;
import com.abetappteam.abetapp.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiYearReportServiceTest extends BaseServiceTest {

    @Mock private SemesterRepository semesterRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CourseIndicatorRepository courseIndicatorRepository;
    @Mock private MeasureRepository measureRepository;
    @Mock private MeasureResultRepository measureResultRepository;
    @Mock private OutcomeRepository outcomeRepository;
    @Mock private PerformanceIndicatorRepository performanceIndicatorRepository;
    @Mock private SectionProgramRepository sectionProgramRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private ScheduleEntryRepository scheduleEntryRepository;

    @InjectMocks
    private MultiYearReportService service;

    private static final Long PROGRAM_ID = 1L;
    private static final Long SEMESTER_ID = 10L;
    private static final Long COURSE_ID = 20L;
    private static final int SECTION_ID = 5;
    private static final Long SCHEDULE_ENTRY_ID = 100L;
    private static final Long COURSE_INDICATOR_ID = 300L;
    private static final Long MEASURE_ID = 400L;
    private static final Long INDICATOR_ID = 200L;
    private static final Long OUTCOME_ID = 50L;

    private Semester testSemester;
    private SectionProgram testSectionProgram;
    private Section testSection;
    private Course testCourse;
    private ScheduleEntry testScheduleEntry;
    private CourseIndicator testCourseIndicator;
    private Measure testMeasure;
    private MeasureResult testMeasureResult;
    private PerformanceIndicator testIndicator;
    private Outcome testOutcome;

    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2023, 9, 1);
        endDate = LocalDate.of(2024, 5, 31);

        testSemester = TestDataBuilder.createSemesterWithId(
                SEMESTER_ID, "Fall 2023", "FALL-2023",
                startDate, endDate, 2023, Semester.SemesterType.FALL, PROGRAM_ID, "Test semester", true);

        testSectionProgram = new SectionProgram(SECTION_ID, PROGRAM_ID.intValue());
        testSectionProgram.setId(1L);

        testSection = new Section("001", COURSE_ID.intValue(), SEMESTER_ID.intValue());
        testSection.setId((long) SECTION_ID);

        testCourse = TestDataBuilder.createCourseWithId(COURSE_ID, "CS101", "Intro to CS", "Description", null);
        testCourse.setIsActive(true);
        testCourse.setStudentCount(30);

        testScheduleEntry = new ScheduleEntry(
                SEMESTER_ID.intValue(), COURSE_ID.intValue(), PROGRAM_ID.intValue(), INDICATOR_ID.intValue());
        testScheduleEntry.setId(SCHEDULE_ENTRY_ID);

        testCourseIndicator = TestDataBuilder.createCourseIndicator(COURSE_INDICATOR_ID, COURSE_ID, INDICATOR_ID, true);

        testMeasure = TestDataBuilder.createMeasureWithId(
                MEASURE_ID, SCHEDULE_ENTRY_ID, "Test measure description", "Improve X", true);

        testMeasureResult = new MeasureResult(MEASURE_ID, 1L, 8, 2, 0, "Good observation", null, "APPROVED");
        testMeasureResult.setId(1L);

        testIndicator = new PerformanceIndicator("Indicator desc", 1, OUTCOME_ID, PROGRAM_ID);
        testIndicator.setId(INDICATOR_ID);
        testIndicator.setIsActive(true);

        testOutcome = TestDataBuilder.createOutcomeWithId(
                OUTCOME_ID, 1, "Student Outcome 1", 1L, PROGRAM_ID, 80, null, true);
    }

    // ─── getSemestersInDateRange ──────────────────────────────────────────────────

    @Test
    void getSemestersInDateRange_throwsWhenEndDateBeforeStartDate() {
        assertThatThrownBy(() -> service.getSemestersInDateRange(PROGRAM_ID, endDate, startDate))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("End date cannot be before start date");
    }

    @Test
    void getSemestersInDateRange_returnsOnlySemestersMatchingProgramId() {
        Semester otherProgramSemester = TestDataBuilder.createSemesterWithId(
                99L, "Fall 2023 Other", "FALL-2023-X",
                startDate, endDate, 2023, Semester.SemesterType.FALL, 999L, "Other", false);

        when(semesterRepository.searchSemesters(null, null, null, startDate, endDate, null, null, null))
                .thenReturn(List.of(testSemester, otherProgramSemester));

        List<Semester> result = service.getSemestersInDateRange(PROGRAM_ID, startDate, endDate);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(SEMESTER_ID);
    }

    @Test
    void getSemestersInDateRange_returnsEmptyListWhenNoneMatchProgram() {
        when(semesterRepository.searchSemesters(null, null, null, startDate, endDate, null, null, null))
                .thenReturn(List.of());

        List<Semester> result = service.getSemestersInDateRange(PROGRAM_ID, startDate, endDate);

        assertThat(result).isEmpty();
    }

    // ─── getMeasuresInDateRange ───────────────────────────────────────────────────

    @Test
    void getMeasuresInDateRange_returnsMeasuresForMatchingSemesters() {
        Measure m1 = TestDataBuilder.createMeasureWithId(1L, SCHEDULE_ENTRY_ID, "Desc1", "Action1", true);
        Measure m2 = TestDataBuilder.createMeasureWithId(2L, SCHEDULE_ENTRY_ID, "Desc2", "Action2", true);

        when(semesterRepository.searchSemesters(null, null, null, startDate, endDate, null, null, null))
                .thenReturn(List.of(testSemester));
        when(measureRepository.findMeasuresByProgramAndSemesters(
                PROGRAM_ID.intValue(), List.of(SEMESTER_ID.intValue()), true))
                .thenReturn(List.of(m1, m2));

        List<Measure> result = service.getMeasuresInDateRange(PROGRAM_ID, startDate, endDate);

        assertThat(result).hasSize(2);
        verify(measureRepository).findMeasuresByProgramAndSemesters(
                PROGRAM_ID.intValue(), List.of(SEMESTER_ID.intValue()), true);
    }

    @Test
    void getMeasuresInDateRange_returnsEmptyListWhenNoSemestersFound() {
        when(semesterRepository.searchSemesters(null, null, null, startDate, endDate, null, null, null))
                .thenReturn(List.of());
        when(measureRepository.findMeasuresByProgramAndSemesters(PROGRAM_ID.intValue(), List.of(), true))
                .thenReturn(List.of());

        List<Measure> result = service.getMeasuresInDateRange(PROGRAM_ID, startDate, endDate);

        assertThat(result).isEmpty();
    }

    // ─── buildReportForSemester ───────────────────────────────────────────────────

    @Test
    void buildReportForSemester_throwsWhenSemesterNotFound() {
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buildReportForSemester(PROGRAM_ID, SEMESTER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Semester not found");
    }

    @Test
    void buildReportForSemester_throwsWhenNoProgramSectionsExist() {
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(testSemester));
        when(sectionProgramRepository.findByProgramId(PROGRAM_ID.intValue())).thenReturn(List.of());

        assertThatThrownBy(() -> service.buildReportForSemester(PROGRAM_ID, SEMESTER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No courses found");
    }

    @Test
    void buildReportForSemester_throwsWhenSectionBelongsToDifferentSemester() {
        Section wrongSemesterSection = new Section("001", COURSE_ID.intValue(), 999);
        wrongSemesterSection.setId((long) SECTION_ID);

        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(testSemester));
        when(sectionProgramRepository.findByProgramId(PROGRAM_ID.intValue()))
                .thenReturn(List.of(testSectionProgram));
        when(sectionRepository.findById((long) SECTION_ID)).thenReturn(Optional.of(wrongSemesterSection));

        assertThatThrownBy(() -> service.buildReportForSemester(PROGRAM_ID, SEMESTER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No courses found");
    }

    @Test
    void buildReportForSemester_throwsWhenAllCoursesAreInactive() {
        testCourse.setIsActive(false);

        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(testSemester));
        when(sectionProgramRepository.findByProgramId(PROGRAM_ID.intValue()))
                .thenReturn(List.of(testSectionProgram));
        when(sectionRepository.findById((long) SECTION_ID)).thenReturn(Optional.of(testSection));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(testCourse));

        assertThatThrownBy(() -> service.buildReportForSemester(PROGRAM_ID, SEMESTER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No measures found");
    }

    @Test
    void buildReportForSemester_buildsCompleteReportSuccessfully() {
        setupHappyPathMocks();

        MultiYearReportData report = service.buildReportForSemester(PROGRAM_ID, SEMESTER_ID);

        assertThat(report).isNotNull();
        assertThat(report.getSemesterId()).isEqualTo(SEMESTER_ID);
        assertThat(report.getAcademicYear()).isEqualTo("2023–2024");
        assertThat(report.getOutcomes()).hasSize(1);

        OutcomeReportData outcome = report.getOutcomes().get(0);
        assertThat(outcome.getOutcomeId()).isEqualTo(OUTCOME_ID);
        assertThat(outcome.getOutcomeNumber()).isEqualTo(1);
        assertThat(outcome.getIndicators()).hasSize(1);

        IndicatorReportData indicator = outcome.getIndicators().get(0);
        assertThat(indicator.getCourseCode()).isEqualTo("CS101");
        assertThat(indicator.getMeasures()).hasSize(1);

        ReportMeasureData measure = indicator.getMeasures().get(0);
        assertThat(measure.getMeasureId()).isEqualTo(MEASURE_ID);
        assertThat(measure.getStudentsMet()).isEqualTo(8);
        assertThat(measure.getStudentsExceeded()).isEqualTo(2);
        assertThat(measure.getStudentsBelow()).isEqualTo(0);
    }

    @Test
    void buildReportForSemester_usesOutcomeEvaluationOverComputedStatusWhenPresent() {
        testOutcome = TestDataBuilder.createOutcomeWithId(
                OUTCOME_ID, 1, "Student Outcome 1", 1L, PROGRAM_ID, 80, "Custom Evaluation", true);
        setupHappyPathMocks();

        MultiYearReportData report = service.buildReportForSemester(PROGRAM_ID, SEMESTER_ID);

        assertThat(report.getOutcomes().get(0).getOverallStatus()).isEqualTo("Custom Evaluation");
    }

    @Test
    void buildReportForSemester_includesRecommendedActionsFromMeasures() {
        setupHappyPathMocks();

        MultiYearReportData report = service.buildReportForSemester(PROGRAM_ID, SEMESTER_ID);

        assertThat(report.getOutcomes().get(0).getRecommendedActions()).contains("Improve X");
    }

    @Test
    void buildReportForSemester_deduplicatesMeasureAppearsInBothScheduleEntryAndCourseIndicatorPaths() {
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(testSemester));
        when(sectionProgramRepository.findByProgramId(PROGRAM_ID.intValue()))
                .thenReturn(List.of(testSectionProgram));
        when(sectionRepository.findById((long) SECTION_ID)).thenReturn(Optional.of(testSection));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(testCourse));
        when(scheduleEntryRepository.findByCourseId(COURSE_ID.intValue()))
                .thenReturn(List.of(testScheduleEntry));
        when(courseIndicatorRepository.findByCourseIdAndIsActive(COURSE_ID, true))
                .thenReturn(List.of(testCourseIndicator));
        when(measureRepository.findByScheduleEntryId(SCHEDULE_ENTRY_ID))
                .thenReturn(List.of(testMeasure));
        // Same measure returned by the course_indicator path (duplicate)
        when(measureRepository.findByCourseIndicatorId(COURSE_INDICATOR_ID))
                .thenReturn(List.of(testMeasure));
        when(performanceIndicatorRepository.findById(INDICATOR_ID))
                .thenReturn(Optional.of(testIndicator));
        when(outcomeRepository.findById(OUTCOME_ID)).thenReturn(Optional.of(testOutcome));
        when(measureResultRepository.findByMeasureId(MEASURE_ID))
                .thenReturn(List.of(testMeasureResult));

        MultiYearReportData report = service.buildReportForSemester(PROGRAM_ID, SEMESTER_ID);

        assertThat(report.getOutcomes().get(0).getIndicators().get(0).getMeasures()).hasSize(1);
    }

    // ─── Measure status thresholds (tested via buildReportForSemester) ────────────

    @Test
    void measureStatus_isMetComfortably_whenAtLeast80PercentMet() {
        MultiYearReportData report = buildReportWithStudentCounts(8, 0, 2); // 80%
        assertThat(getMeasureStatus(report)).isEqualTo("Met comfortably");
    }

    @Test
    void measureStatus_isMetComfortably_whenExceededStudentsPushPercentageAbove80() {
        MultiYearReportData report = buildReportWithStudentCounts(0, 9, 1); // 90%
        assertThat(getMeasureStatus(report)).isEqualTo("Met comfortably");
    }

    @Test
    void measureStatus_isMet_when70to79PercentMet() {
        MultiYearReportData report = buildReportWithStudentCounts(7, 0, 3); // 70%
        assertThat(getMeasureStatus(report)).isEqualTo("Met");
    }

    @Test
    void measureStatus_isBarelyNotMet_when65to69PercentMet() {
        MultiYearReportData report = buildReportWithStudentCounts(13, 0, 7); // 65%
        assertThat(getMeasureStatus(report)).isEqualTo("Barely not met");
    }

    @Test
    void measureStatus_isNotMet_whenBelow65Percent() {
        MultiYearReportData report = buildReportWithStudentCounts(6, 0, 4); // 60%
        assertThat(getMeasureStatus(report)).isEqualTo("Not met");
    }

    @Test
    void measureStatus_isNotMet_whenZeroStudents() {
        MultiYearReportData report = buildReportWithStudentCounts(0, 0, 0);
        assertThat(getMeasureStatus(report)).isEqualTo("Not met");
    }

    // ─── Outcome status thresholds (tested via buildReportForSemester) ────────────

    @Test
    void outcomeStatus_isMET_whenComputedAndAtLeast80PercentOfMeasuresMet() {
        // Single measure at 80% → "Met comfortably" → 1/1 = 100% → "MET"
        MultiYearReportData report = buildReportWithStudentCounts(8, 0, 2);
        assertThat(report.getOutcomes().get(0).getOverallStatus()).isEqualTo("MET");
    }

    @Test
    void outcomeStatus_isNotMet_whenComputedAndLessThan50PercentOfMeasuresMet() {
        // Single measure at 60% → "Not met" → 0/1 = 0% → "Not Met"
        MultiYearReportData report = buildReportWithStudentCounts(6, 0, 4);
        assertThat(report.getOutcomes().get(0).getOverallStatus()).isEqualTo("Not Met");
    }

    // ─── buildReportByAcademicYear ────────────────────────────────────────────────

    @Test
    void buildReportByAcademicYear_throwsWhenNoSemestersInRange() {
        when(semesterRepository.searchSemesters(null, null, null, startDate, endDate, null, null, null))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.buildReportByAcademicYear(PROGRAM_ID, startDate, endDate))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No semesters found");
    }

    @Test
    void buildReportByAcademicYear_returnsOneReportPerAcademicYear() {
        when(semesterRepository.searchSemesters(null, null, null, startDate, endDate, null, null, null))
                .thenReturn(List.of(testSemester));
        setupBuildForSemestersChain();

        List<MultiYearReportData> reports = service.buildReportByAcademicYear(PROGRAM_ID, startDate, endDate);

        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).getAcademicYear()).isEqualTo("2023–2024");
        assertThat(reports.get(0).getOutcomes()).isNotEmpty();
    }

    @Test
    void buildReportByAcademicYear_includesEmptyPlaceholderForYearWithNoData() {
        Semester semesterYear2 = TestDataBuilder.createSemesterWithId(
                20L, "Fall 2024", "FALL-2024",
                LocalDate.of(2024, 9, 1), LocalDate.of(2025, 5, 31),
                2024, Semester.SemesterType.FALL, PROGRAM_ID, "Year 2", false);

        when(semesterRepository.searchSemesters(null, null, null, startDate, endDate, null, null, null))
                .thenReturn(List.of(testSemester, semesterYear2));

        // Year 2023: full chain; year 2024: no section programs → exception → empty placeholder
        when(sectionProgramRepository.findByProgramId(PROGRAM_ID.intValue()))
                .thenReturn(List.of(testSectionProgram))
                .thenReturn(List.of());
        when(sectionRepository.findById((long) SECTION_ID)).thenReturn(Optional.of(testSection));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(testCourse));
        when(scheduleEntryRepository.findByCourseId(COURSE_ID.intValue()))
                .thenReturn(List.of(testScheduleEntry));
        when(courseIndicatorRepository.findByCourseIdAndIsActive(COURSE_ID, true))
                .thenReturn(List.of(testCourseIndicator));
        when(measureRepository.findByScheduleEntryId(SCHEDULE_ENTRY_ID))
                .thenReturn(List.of(testMeasure));
        when(measureRepository.findByCourseIndicatorId(COURSE_INDICATOR_ID))
                .thenReturn(List.of());
        when(performanceIndicatorRepository.findById(INDICATOR_ID))
                .thenReturn(Optional.of(testIndicator));
        when(outcomeRepository.findById(OUTCOME_ID)).thenReturn(Optional.of(testOutcome));
        when(measureResultRepository.findByMeasureId(MEASURE_ID))
                .thenReturn(List.of(testMeasureResult));

        List<MultiYearReportData> reports = service.buildReportByAcademicYear(PROGRAM_ID, startDate, endDate);

        assertThat(reports).hasSize(2);
        assertThat(reports.get(0).getOutcomes()).isNotEmpty();
        assertThat(reports.get(1).getOutcomes()).isEmpty();
    }

    // ─── buildHierarchicalReport ──────────────────────────────────────────────────

    @Test
    void buildHierarchicalReport_throwsWhenNoSemestersInRange() {
        when(semesterRepository.searchSemesters(null, null, null, startDate, endDate, null, null, null))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.buildHierarchicalReport(PROGRAM_ID, startDate, endDate))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No semesters found");
    }

    @Test
    void buildHierarchicalReport_buildsAggregatedReportAcrossEntireDateRange() {
        when(semesterRepository.searchSemesters(null, null, null, startDate, endDate, null, null, null))
                .thenReturn(List.of(testSemester));
        setupBuildForSemestersChain();

        MultiYearReportData report = service.buildHierarchicalReport(PROGRAM_ID, startDate, endDate);

        assertThat(report).isNotNull();
        assertThat(report.getAcademicYear()).isEqualTo("2023–2024");
        assertThat(report.getOutcomes()).hasSize(1);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private void setupHappyPathMocks() {
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(testSemester));
        setupBuildForSemestersChain();
    }

    private void setupBuildForSemestersChain() {
        when(sectionProgramRepository.findByProgramId(PROGRAM_ID.intValue()))
                .thenReturn(List.of(testSectionProgram));
        when(sectionRepository.findById((long) SECTION_ID)).thenReturn(Optional.of(testSection));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(testCourse));
        when(scheduleEntryRepository.findByCourseId(COURSE_ID.intValue()))
                .thenReturn(List.of(testScheduleEntry));
        when(courseIndicatorRepository.findByCourseIdAndIsActive(COURSE_ID, true))
                .thenReturn(List.of(testCourseIndicator));
        when(measureRepository.findByScheduleEntryId(SCHEDULE_ENTRY_ID))
                .thenReturn(List.of(testMeasure));
        when(measureRepository.findByCourseIndicatorId(COURSE_INDICATOR_ID))
                .thenReturn(List.of());
        when(performanceIndicatorRepository.findById(INDICATOR_ID))
                .thenReturn(Optional.of(testIndicator));
        when(outcomeRepository.findById(OUTCOME_ID)).thenReturn(Optional.of(testOutcome));
        when(measureResultRepository.findByMeasureId(MEASURE_ID))
                .thenReturn(List.of(testMeasureResult));
    }

    private MultiYearReportData buildReportWithStudentCounts(int met, int exceeded, int below) {
        testMeasureResult = new MeasureResult(MEASURE_ID, 1L, met, exceeded, below, "obs", null, "APPROVED");
        testMeasureResult.setId(1L);
        setupHappyPathMocks();
        return service.buildReportForSemester(PROGRAM_ID, SEMESTER_ID);
    }

    private String getMeasureStatus(MultiYearReportData report) {
        return report.getOutcomes().get(0).getIndicators().get(0).getMeasures().get(0).getStatus();
    }
}
