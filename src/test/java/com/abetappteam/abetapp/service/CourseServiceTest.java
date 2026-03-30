package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.BaseServiceTest;
import com.abetappteam.abetapp.dto.CourseDTO;
import com.abetappteam.abetapp.entity.Course;
import com.abetappteam.abetapp.entity.CourseIndicator;
import com.abetappteam.abetapp.entity.CourseInstructor;
import com.abetappteam.abetapp.entity.Requests.Course.CourseSearchRequest;
import com.abetappteam.abetapp.exception.BusinessException;
import com.abetappteam.abetapp.exception.ConflictException;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.repository.CourseRepository;
import com.abetappteam.abetapp.repository.CourseIndicatorRepository;
import com.abetappteam.abetapp.repository.CourseInstructorRepository;
import com.abetappteam.abetapp.repository.MeasureRepository;
import com.abetappteam.abetapp.repository.MeasureResultRepository;
import com.abetappteam.abetapp.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourseService
 */
class CourseServiceTest extends BaseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseIndicatorRepository courseIndicatorRepository;

    @Mock
    private CourseInstructorRepository courseInstructorRepository;

    @Mock
    private MeasureResultRepository measureResultRepository;

    @Mock
    private MeasureRepository measureRepository;

    @InjectMocks
    private CourseService courseService;

    private Course testCourse;
    private CourseDTO testCourseDTO;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(courseRepository);

        ReflectionTestUtils.setField(courseService, "courseInstructorRepository", courseInstructorRepository);

        ReflectionTestUtils.setField(courseService, "courseIndicatorRepository", courseIndicatorRepository);

        ReflectionTestUtils.setField(courseService, "measureResultRepository", measureResultRepository);

        ReflectionTestUtils.setField(courseService, "measureRepository", measureRepository);

        testCourse = TestDataBuilder.createCourseWithId(1L, "CS101", "Introduction to Computer Science",
                "Basic computer science principles", 1L);
        testCourse.setStudentCount(30);

        testCourseDTO = TestDataBuilder.createCourseDTO("CS102", "Database Systems",
                "Database management systems", 1, 1.0);
        testCourseDTO.setStudentCount(25);
    }

    @Test
    void shouldFindById() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        // When
        Course found = courseService.findById(1L);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
        assertThat(found.getCourseName()).isEqualTo("Introduction to Computer Science");
        assertThat(found.getCourseCode()).isEqualTo("CS101");
        verify(courseRepository).findById(1L);
    }

    @Test
    void shouldGetAllActiveCourses() {
        when(courseRepository.findByIsActive(true)).thenReturn(List.of(testCourse));

        List<Course> results = courseService.getAllActiveCourses();

        assertThat(results).hasSize(1);
        verify(courseRepository).findByIsActive(true);
    }

    @Test
    void shouldGetActiveCoursesByProgramUserId() {
        when(courseRepository.findActiveCoursesByProgramUserId(5L)).thenReturn(List.of(testCourse));

        List<Course> results = courseService.getActiveCoursesByProgramUserId(5L);

        assertThat(results).hasSize(1);
        verify(courseRepository).findActiveCoursesByProgramUserId(5L);
    }

    @Test
    void shouldThrowExceptionWhenNotFound() {
        // Given
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> courseService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found with id: 999");
    }

    @Test
    void shouldCreateCourse() {
        // Given
        when(courseRepository.existsByCourseCodeIgnoreCase("CS102")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // When
        Course created = courseService.createCourse(testCourseDTO);

        // Then
        assertThat(created).isNotNull();
        verify(courseRepository).existsByCourseCodeIgnoreCase("CS102");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void shouldThrowConflictWhenCreatingDuplicate() {
        // Given
        when(courseRepository.existsByCourseCodeIgnoreCase("CS102")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> courseService.createCourse(testCourseDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists in this semester");
        verify(courseRepository, never()).save(any());
    }

    @Test
    void shouldSearchCourses() {
        // Given
        CourseSearchRequest request = new CourseSearchRequest(null, null, null, null, null, null, true);
        when(courseRepository.searchCourse(any(), any(), any(), any(), any(), any(), any(Boolean.class)))
                .thenReturn(List.of(testCourse));

        // When
        List<Course> results = courseService.searchCourse(request);

        // Then
        assertThat(results).hasSize(1);
        verify(courseRepository).searchCourse(any(), any(), any(), any(), any(), any(), any(Boolean.class));
    }

    @Test
    void shouldUpdateCourse() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.existsByCourseCodeIgnoreCase("CS102")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // When
        Course updated = courseService.updateCourse(1L, testCourseDTO);

        // Then
        assertThat(updated).isNotNull();
        verify(courseRepository).findById(1L);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void shouldUpdateStudentCount() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // When
        Course updated = courseService.updateStudentCount(1L, 35);

        // Then
        assertThat(updated).isNotNull();
        assertThat(testCourse.getStudentCount()).isEqualTo(35);
        verify(courseRepository).findById(1L);
        verify(courseRepository).save(testCourse);
    }

    @Test
    void shouldNotCheckDuplicateWhenCourseCodeUnchanged() {
        // Given
        Course existingCourse = TestDataBuilder.createCourseWithId(1L, "CS102", "Some Course",
                "Description", 1L);
        CourseDTO updateDTO = TestDataBuilder.createCourseDTO("CS102", "Updated Name",
                "Updated Description", 1, 1.0);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(existingCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(existingCourse);

        // When
        Course updated = courseService.updateCourse(1L, updateDTO);

        // Then
        assertThat(updated).isNotNull();
        verify(courseRepository).findById(1L);
        verify(courseRepository, never()).existsByCourseCodeIgnoreCase(anyString());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void shouldThrowConflictWhenUpdatingWithDuplicateCourseCode() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.existsByCourseCodeIgnoreCase("CS102")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> courseService.updateCourse(1L, testCourseDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
        verify(courseRepository, never()).save(any());
    }

    @Test
    void shouldDeleteCourse() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.countMeasuresInReviewByCourseId(1L)).thenReturn(0);
        when(courseIndicatorRepository.findByCourseId(1L)).thenReturn(List.of());

        // When
        courseService.removeCourse(1L);

        // Then
        verify(courseIndicatorRepository).findByCourseId(1L);
        verify(courseIndicatorRepository).deleteByCourseId(1L);
        verify(courseInstructorRepository).deleteByCourseId(1L);
        verify(courseRepository).delete(testCourse);
    }

    @Test
    void shouldThrowExceptionWhenDeletingCourseWithMeasuresInReview() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.countMeasuresInReviewByCourseId(1L)).thenReturn(2);

        // When/Then
        assertThatThrownBy(() -> courseService.removeCourse(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete course with measures submitted for review");
        verify(courseRepository, never()).delete(any());
    }

    @Test
    void shoudDeleteCourse() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.countMeasuresInReviewByCourseId(1L)).thenReturn(0);
        doNothing().when(courseRepository).delete(testCourse);

        // When
        courseService.removeCourse(1L);

        // Then
        verify(courseRepository).findById(1L);
        verify(courseRepository).delete(testCourse);
    }

    @Test
    void shouldDeactivateCourse() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // When
        courseService.deactivateCourse(1L);

        // Then
        verify(courseRepository).findById(1L);
        verify(courseRepository).save(testCourse);
        assertThat(testCourse.getIsActive()).isFalse();
    }

    @Test
    void shouldActivateCourse() {
        // Given
        testCourse.setIsActive(false);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // When
        courseService.activateCourse(1L);

        // Then
        verify(courseRepository).findById(1L);
        verify(courseRepository).save(testCourse);
        assertThat(testCourse.getIsActive()).isTrue();
    }

    @Test
    void shouldFindByCourseCode() {
        // Given
        when(courseRepository.findByCourseCodeIgnoreCase("CS101")).thenReturn(Optional.of(testCourse));

        // When
        Course found = courseService.findByCourseCode("CS101");

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getCourseCode()).isEqualTo("CS101");
        assertThat(found.getCourseName()).isEqualTo("Introduction to Computer Science");
    }

    @Test
    void shouldThrowExceptionWhenCourseCodeNotFound() {
        // Given
        when(courseRepository.findByCourseCodeIgnoreCase("UNKNOWN")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> courseService.findByCourseCode("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found with code: UNKNOWN");
    }

    @Test
    void shouldAssignInstructor() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseInstructorRepository.existsByCourseIdAndProgramUserId(1L, 2L)).thenReturn(false);

        courseService.assignInstructor(1L, 2L);

        verify(courseInstructorRepository).save(any(CourseInstructor.class));
    }

    @Test
    void shouldThrowWhenAssigningDuplicateInstructor() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseInstructorRepository.existsByCourseIdAndProgramUserId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> courseService.assignInstructor(1L, 2L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Instructor already assigned");

        verify(courseInstructorRepository, never()).save(any());
    }

    @Test
    void shouldRemoveInstructor() {
        CourseInstructor ci = new CourseInstructor(2L, 1L);
        when(courseInstructorRepository.findByCourseIdAndProgramUserId(1L, 2L)).thenReturn(Optional.of(ci));

        courseService.removeInstructor(1L, 2L);

        assertThat(ci.getIsActive()).isFalse();
        verify(courseInstructorRepository).save(ci);
    }

    @Test
    void shouldThrowWhenRemovingNonExistentInstructor() {
        when(courseInstructorRepository.findByCourseIdAndProgramUserId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.removeInstructor(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Instructor assignment not found");
    }

    @Test
    void shouldGetInstructorIds() {
        CourseInstructor ci = new CourseInstructor(2L, 1L);
        when(courseInstructorRepository.findByCourseIdAndIsActive(1L, true)).thenReturn(List.of(ci));

        List<Long> ids = courseService.getInstructorIds(1L);

        assertThat(ids).containsExactly(2L);
        verify(courseInstructorRepository).findByCourseIdAndIsActive(1L, true);
    }

    @Test
    void shouldAssignIndicator() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseIndicatorRepository.existsByCourseIdAndIndicatorId(1L, 3L)).thenReturn(false);

        courseService.assignIndicator(1L, 3L);

        verify(courseIndicatorRepository).save(any(CourseIndicator.class));
    }

    @Test
    void shouldThrowWhenAssigningDuplicateIndicator() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseIndicatorRepository.existsByCourseIdAndIndicatorId(1L, 3L)).thenReturn(true);

        assertThatThrownBy(() -> courseService.assignIndicator(1L, 3L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Indicator already assigned");

        verify(courseIndicatorRepository, never()).save(any());
    }

    @Test
    void shouldRemoveIndicator() {
        CourseIndicator ci = new CourseIndicator(1L, 3L);
        when(courseIndicatorRepository.findByCourseIdAndIndicatorId(1L, 3L)).thenReturn(Optional.of(ci));

        courseService.removeIndicator(1L, 3L);

        assertThat(ci.getIsActive()).isFalse();
        verify(courseIndicatorRepository).save(ci);
    }

    @Test
    void shouldThrowWhenRemovingNonExistentIndicator() {
        when(courseIndicatorRepository.findByCourseIdAndIndicatorId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.removeIndicator(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Indicator assignment not found");
    }

    @Test
    void shouldGetIndicatorIds() {
        CourseIndicator ci = new CourseIndicator(1L, 3L);
        when(courseIndicatorRepository.findByCourseIdAndIsActive(1L, true)).thenReturn(List.of(ci));

        List<Long> ids = courseService.getIndicatorIds(1L);

        assertThat(ids).containsExactly(3L);
        verify(courseIndicatorRepository).findByCourseIdAndIsActive(1L, true);
    }

    @Test
    void shouldGetCourseIndicatorByCourseIdAndIndicatorId() {
        CourseIndicator ci = new CourseIndicator(1L, 3L);
        when(courseIndicatorRepository.findByCourseIdAndIndicatorId(1L, 3L)).thenReturn(Optional.of(ci));

        Optional<CourseIndicator> result = courseService.getCourseIndicatorByCourseIdAndIndicatorId(1L, 3L);

        assertThat(result).isPresent();
        verify(courseIndicatorRepository).findByCourseIdAndIndicatorId(1L, 3L);
    }

    @Test
    void shouldGetCourseIndicatorById() {
        CourseIndicator ci = new CourseIndicator(1L, 3L);
        ci.setId(10L);
        when(courseIndicatorRepository.findById(10L)).thenReturn(Optional.of(ci));

        Optional<CourseIndicator> result = courseService.getCourseIndicatorById(10L);

        assertThat(result).isPresent();
        verify(courseIndicatorRepository).findById(10L);
    }

    @Test
    void shouldCheckExistsByCourseCode() {
        // Given
        when(courseRepository.existsByCourseCodeIgnoreCase("CS101")).thenReturn(true);

        // When
        boolean exists = courseService.existsByCourseCode("CS101");

        // Then
        assertThat(exists).isTrue();
        verify(courseRepository).existsByCourseCodeIgnoreCase("CS101");
    }

    @Test
    void shouldCalculateMeasureCompleteness() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.countTotalMeasuresByCourseId(1L)).thenReturn(10);
        when(courseRepository.countCompletedMeasuresByCourseId(1L)).thenReturn(5);
        when(courseRepository.countInProgressMeasuresByCourseId(1L)).thenReturn(3);
        when(courseRepository.countSubmittedMeasuresByCourseId(1L)).thenReturn(2);

        // When
        CourseService.MeasureCompletenessResponse response = courseService.calculateMeasureCompleteness(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCourseId()).isEqualTo(1L);
        assertThat(response.getTotalMeasures()).isEqualTo(10);
        assertThat(response.getCompletedMeasures()).isEqualTo(5);
        assertThat(response.getInProgressMeasures()).isEqualTo(3);
        assertThat(response.getSubmittedMeasures()).isEqualTo(2);
        assertThat(response.getCompletionPercentage()).isEqualTo(50.0);
    }

    @Test
    void shouldReturnZeroCompletionPercentageWhenNoMeasures() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.countTotalMeasuresByCourseId(1L)).thenReturn(0);
        when(courseRepository.countCompletedMeasuresByCourseId(1L)).thenReturn(0);
        when(courseRepository.countInProgressMeasuresByCourseId(1L)).thenReturn(0);
        when(courseRepository.countSubmittedMeasuresByCourseId(1L)).thenReturn(0);

        // When
        CourseService.MeasureCompletenessResponse response = courseService.calculateMeasureCompleteness(1L);

        // Then
        assertThat(response.getCompletionPercentage()).isEqualTo(0.0);
    }

    @Test
    void shouldVersionCourse() {
        // Given
        CourseDTO versionDTO = TestDataBuilder.createCourseDTO("CS101-V2", "Intro to CS v2",
                "Updated description", 1, 1.0);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.existsByCourseCodeIgnoreCase("CS101-V2")).thenReturn(false);
        Course newVersion = TestDataBuilder.createCourseWithId(2L, "CS101-V2", "Intro to CS v2",
                "Updated description", 1L);
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse).thenReturn(newVersion);

        // When
        Course result = courseService.versionCourse(1L, versionDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(testCourse.getIsActive()).isFalse();
        verify(courseRepository, times(2)).save(any(Course.class));
    }

    @Test
    void shouldThrowConflictWhenVersioningWithDuplicateCode() {
        // Given
        CourseDTO versionDTO = TestDataBuilder.createCourseDTO("CS101", "Same Code",
                "Description", 1, 1.0);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.existsByCourseCodeIgnoreCase("CS101")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> courseService.versionCourse(1L, versionDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists in this semester");
        verify(courseRepository, never()).save(any());
    }
}