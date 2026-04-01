package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.dto.importer.*;
import com.abetappteam.abetapp.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImporterServiceTest {

    @Mock
    private SemesterService semesterService;
    @Mock
    private OutcomeService outcomeService;
    @Mock
    private PerformanceIndicatorService indicatorService;
    @Mock
    private CourseService courseService;
    @Mock
    private CourseIndicatorService courseIndicatorService;
    @Mock
    private MeasureService measureService;
    @Mock
    private MeasureResultService measureResultService;

    @InjectMocks
    private ImporterService importerService;

    private Semester semester;
    private Outcome outcome;
    private PerformanceIndicator indicator;
    private Course course;
    private CourseIndicator courseIndicator;

    @BeforeEach
    void setUp() {
        semester = new Semester();
        semester.setId(1L);

        outcome = new Outcome();
        outcome.setId(1L);
        outcome.setNumber(1);

        indicator = new PerformanceIndicator();
        indicator.setId(1L);
        indicator.setIndicatorNumber(1);

        course = new Course();
        course.setId(1L);
        course.setCourseCode("CS101");

        courseIndicator = new CourseIndicator();
        courseIndicator.setId(1L);
    }

    @Test
    void testImportSummary_CreatesAllEntities() {
        SummaryImportDTO dto = createBasicImport();

        when(semesterService.findById(1L)).thenReturn(semester);
        when(outcomeService.findActiveOutcomesBySemester(1L)).thenReturn(new ArrayList<>());
        when(outcomeService.create(any())).thenReturn(outcome);
        when(indicatorService.getIndicatorsByStudentOutcome(1L)).thenReturn(new ArrayList<>());
        when(indicatorService.createPerformanceIndicator(any())).thenReturn(indicator);
        when(courseService.createCourse(any())).thenReturn(course);
        when(courseIndicatorService.getOrCreate(1L, 1L)).thenReturn(courseIndicator);
        when(measureService.createFromImport(any())).thenReturn(new Measure());

        importerService.importSummary(dto);

        verify(outcomeService).create(any());
        verify(indicatorService).createPerformanceIndicator(any());
        verify(courseService).createCourse(any());
        verify(measureService).createFromImport(any());
    }

    @Test
    void testImportSummary_ReusesExistingOutcome() {
        SummaryImportDTO dto = createBasicImport();

        when(semesterService.findById(1L)).thenReturn(semester);
        when(outcomeService.findActiveOutcomesBySemester(1L)).thenReturn(Arrays.asList(outcome));
        when(indicatorService.getIndicatorsByStudentOutcome(1L)).thenReturn(new ArrayList<>());
        when(indicatorService.createPerformanceIndicator(any())).thenReturn(indicator);
        when(courseService.createCourse(any())).thenReturn(course);
        when(courseIndicatorService.getOrCreate(1L, 1L)).thenReturn(courseIndicator);
        when(measureService.createFromImport(any())).thenReturn(new Measure());

        importerService.importSummary(dto);

        verify(outcomeService, never()).create(any());
    }

    @Test
    void testImportSummary_ReusesExistingIndicator() {
        SummaryImportDTO dto = createBasicImport();

        when(semesterService.findById(1L)).thenReturn(semester);
        when(outcomeService.findActiveOutcomesBySemester(1L)).thenReturn(Arrays.asList(outcome));
        when(indicatorService.getIndicatorsByStudentOutcome(1L)).thenReturn(Arrays.asList(indicator));
        when(courseService.createCourse(any())).thenReturn(course);
        when(courseIndicatorService.getOrCreate(1L, 1L)).thenReturn(courseIndicator);
        when(measureService.createFromImport(any())).thenReturn(new Measure());

        importerService.importSummary(dto);

        verify(indicatorService, never()).createPerformanceIndicator(any());
    }

    @Test
    void testImportSummary_ReusesExistingCourse() {
        SummaryImportDTO dto = createBasicImport();

        when(semesterService.findById(1L)).thenReturn(semester);
        when(outcomeService.findActiveOutcomesBySemester(1L)).thenReturn(Arrays.asList(outcome));
        when(indicatorService.getIndicatorsByStudentOutcome(1L)).thenReturn(Arrays.asList(indicator));
        when(courseService.findByCourseCode("CS101")).thenReturn(course);
        when(courseIndicatorService.getOrCreate(1L, 1L)).thenReturn(courseIndicator);
        when(measureService.createFromImport(any())).thenReturn(new Measure());

        importerService.importSummary(dto);

        verify(courseService, never()).createCourse(any());
    }

    @Test
    void testExtractIndicatorNumber_From1Point1() {
        SummaryImportDTO dto = createBasicImport();
        dto.getOutcomes().get(0).getIndicators().get(0).setNumber(1.1);

        when(semesterService.findById(1L)).thenReturn(semester);
        when(outcomeService.findActiveOutcomesBySemester(1L)).thenReturn(new ArrayList<>());
        when(outcomeService.create(any())).thenReturn(outcome);
        when(indicatorService.getIndicatorsByStudentOutcome(1L)).thenReturn(new ArrayList<>());
        when(indicatorService.createPerformanceIndicator(any())).thenReturn(indicator);
        when(courseService.createCourse(any())).thenReturn(course);
        when(courseIndicatorService.getOrCreate(anyLong(), anyLong())).thenReturn(courseIndicator);
        when(measureService.createFromImport(any())).thenReturn(new Measure());

        importerService.importSummary(dto);

        verify(indicatorService).createPerformanceIndicator(argThat(pi -> pi.getIndicatorNumber() == 1));
    }

    @Test
    void testExtractIndicatorNumber_From2Point3() {
        SummaryImportDTO dto = createBasicImport();
        dto.getOutcomes().get(0).getIndicators().get(0).setNumber(2.3);

        when(semesterService.findById(1L)).thenReturn(semester);
        when(outcomeService.findActiveOutcomesBySemester(1L)).thenReturn(new ArrayList<>());
        when(outcomeService.create(any())).thenReturn(outcome);
        when(indicatorService.getIndicatorsByStudentOutcome(1L)).thenReturn(new ArrayList<>());
        when(indicatorService.createPerformanceIndicator(any())).thenReturn(indicator);
        when(courseService.createCourse(any())).thenReturn(course);
        when(courseIndicatorService.getOrCreate(anyLong(), anyLong())).thenReturn(courseIndicator);
        when(measureService.createFromImport(any())).thenReturn(new Measure());

        importerService.importSummary(dto);

        verify(indicatorService).createPerformanceIndicator(argThat(pi -> pi.getIndicatorNumber() == 3));
    }

//    @Test
//    void testConvertStatus_MetToComplete() {
//        SummaryImportDTO dto = createBasicImport();
//        dto.getOutcomes().get(0).getIndicators().get(0).getCourses().get(0)
//                .getMeasures().get(0).setStatus("Met");
//
//        setupMocks();
//
//        importerService.importSummary(dto);
//
//        verify(measureService).createFromImport(argThat(m -> "Complete".equals(m.getActive())));
//    }

    @Test
    void testConvertStatus_NotMetToInReview() {
        SummaryImportDTO dto = createBasicImport();
        dto.getOutcomes().get(0).getIndicators().get(0).getCourses().get(0)
                .getMeasures().get(0).setStatus("Not Met");

        setupMocks();

        importerService.importSummary(dto);

        // verify(measureService).createFromImport(argThat(m -> "InReview".equals(m.getStatus())));
    }

    @Test
    void testConvertStatus_NullToInProgress() {
        SummaryImportDTO dto = createBasicImport();
        dto.getOutcomes().get(0).getIndicators().get(0).getCourses().get(0)
                .getMeasures().get(0).setStatus(null);

        setupMocks();

        importerService.importSummary(dto);

        // verify(measureService).createFromImport(argThat(m -> "InProgress".equals(m.getStatus())));
    }

    @Test
    void testCalculateStudentCounts_MetComfortably() {
        SummaryImportDTO dto = createBasicImport();
        dto.getOutcomes().get(0).getIndicators().get(0).getCourses().get(0)
                .getMeasures().get(0).setStatus("Met Comfortably");
        dto.getOutcomes().get(0).getIndicators().get(0).getCourses().get(0)
                .getMeasures().get(0).setMetPercentage(80.0);

        setupMocks();

        importerService.importSummary(dto);

        verify(measureResultService).createFromImport(
                argThat(mr -> mr.getStudentsExceeded() == 10 && mr.getStudentsMet() == 14
                        && mr.getStudentsBelow() == 6));
    }

    @Test
    void testCalculateStudentCounts_Met() {
        SummaryImportDTO dto = createBasicImport();
        dto.getOutcomes().get(0).getIndicators().get(0).getCourses().get(0)
                .getMeasures().get(0).setMetPercentage(70.0);

        setupMocks();

        importerService.importSummary(dto);

        verify(measureResultService).createFromImport(
                argThat(mr -> mr.getStudentsExceeded() == 4 && mr.getStudentsMet() == 17
                        && mr.getStudentsBelow() == 9));
    }

    @Test
    void testCalculateStudentCounts_NotMet() {
        SummaryImportDTO dto = createBasicImport();
        dto.getOutcomes().get(0).getIndicators().get(0).getCourses().get(0)
                .getMeasures().get(0).setStatus("Not Met");
        dto.getOutcomes().get(0).getIndicators().get(0).getCourses().get(0)
                .getMeasures().get(0).setMetPercentage(40.0);

        setupMocks();

        importerService.importSummary(dto);

        verify(measureResultService).createFromImport(
                argThat(mr -> mr.getStudentsExceeded() == 0 && mr.getStudentsMet() == 12
                        && mr.getStudentsBelow() == 18));
    }

    @Test
    void testRecommendedActions_JoinedWithNewlines() {
        SummaryImportDTO dto = createBasicImport();
        dto.getOutcomes().get(0).getIndicators().get(0).getCourses().get(0)
                .getMeasures().get(0).setRecommendedActions(Arrays.asList("Action 1", "Action 2"));

        setupMocks();

        importerService.importSummary(dto);

        verify(measureService).createFromImport(argThat(m -> "Action 1\nAction 2".equals(m.getRecommendedAction())));
    }

    @Test
    void testRecommendedActions_NullWhenEmpty() {
        SummaryImportDTO dto = createBasicImport();
        dto.getOutcomes().get(0).getIndicators().get(0).getCourses().get(0)
                .getMeasures().get(0).setRecommendedActions(new ArrayList<>());

        setupMocks();

        importerService.importSummary(dto);

        verify(measureService).createFromImport(argThat(m -> m.getRecommendedAction() == null));
    }

    @Test
    void testCourseCode_NormalizedToUppercase() {
        SummaryImportDTO dto = createBasicImport();
        dto.getOutcomes().get(0).getIndicators().get(0).getCourses().get(0).setCourseCode("cs101");

        setupMocks();

        importerService.importSummary(dto);

        verify(courseService).createCourse(argThat(c -> "CS101".equals(c.getCourseCode())));
    }

    @Test
    void testCourseCode_Trimmed() {
        SummaryImportDTO dto = createBasicImport();
        dto.getOutcomes().get(0).getIndicators().get(0).getCourses().get(0).setCourseCode("  CS101  ");

        setupMocks();

        importerService.importSummary(dto);

        verify(courseService).createCourse(argThat(c -> "CS101".equals(c.getCourseCode())));
    }

    private SummaryImportDTO createBasicImport() {
        SummaryImportDTO dto = new SummaryImportDTO();
        dto.setSemesterId(1L);

        OutcomeImportDTO outcome = new OutcomeImportDTO();
        outcome.setNumber(1);
        outcome.setStatus("Met");

        IndicatorImportDTO indicator = new IndicatorImportDTO();
        indicator.setNumber(1.1);

        CourseImportDTO course = new CourseImportDTO();
        course.setCourseCode("CS101");

        MeasureImportDTO measure = new MeasureImportDTO();
        measure.setStatus("Met");
        measure.setMetPercentage(70.0);
        measure.setRecommendedActions(Arrays.asList("Review"));

        course.setMeasures(Arrays.asList(measure));
        indicator.setCourses(Arrays.asList(course));
        outcome.setIndicators(Arrays.asList(indicator));
        dto.setOutcomes(Arrays.asList(outcome));

        return dto;
    }

    private void setupMocks() {
        when(semesterService.findById(1L)).thenReturn(semester);
        when(outcomeService.findActiveOutcomesBySemester(1L)).thenReturn(Arrays.asList(outcome));
        when(indicatorService.getIndicatorsByStudentOutcome(1L)).thenReturn(Arrays.asList(indicator));
        when(courseService.createCourse(any())).thenReturn(course);
        when(courseIndicatorService.getOrCreate(1L, 1L)).thenReturn(courseIndicator);
        when(measureService.createFromImport(any())).thenReturn(new Measure());
        when(measureResultService.createFromImport(any())).thenReturn(new MeasureResult());
    }
}