package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.entity.CourseIndicator;
import com.abetappteam.abetapp.entity.Requests.CourseIndicator.CourseIndicatorSearchRequest;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.repository.CourseIndicatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseIndicatorServiceTest {

    @Mock
    private CourseIndicatorRepository repository;

    @InjectMocks
    private CourseIndicatorService courseIndicatorService;

    private CourseIndicator testCourseIndicator;

    @BeforeEach
    void setUp() {
        testCourseIndicator = new CourseIndicator();
        testCourseIndicator.setId(1L);
        testCourseIndicator.setCourseId(1L);
        testCourseIndicator.setIndicatorId(1L);
        testCourseIndicator.setIsActive(true);
    }

    @Test
    void shouldReturnExistingCourseIndicator() {
        // Given
        when(repository.findByCourseIdAndIndicatorId(1L, 1L))
                .thenReturn(Optional.of(testCourseIndicator));

        // When
        CourseIndicator result = courseIndicatorService.getOrCreate(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCourseId()).isEqualTo(1L);
        assertThat(result.getIndicatorId()).isEqualTo(1L);
        verify(repository).findByCourseIdAndIndicatorId(1L, 1L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldCreateNewCourseIndicatorWhenNotExists() {
        // Given
        when(repository.findByCourseIdAndIndicatorId(1L, 1L))
                .thenReturn(Optional.empty());
        when(repository.save(any(CourseIndicator.class)))
                .thenReturn(testCourseIndicator);

        // When
        CourseIndicator result = courseIndicatorService.getOrCreate(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCourseId()).isEqualTo(1L);
        assertThat(result.getIndicatorId()).isEqualTo(1L);
        assertThat(result.getIsActive()).isTrue();
        verify(repository).findByCourseIdAndIndicatorId(1L, 1L);
        verify(repository).save(any(CourseIndicator.class));
    }

    @Test
    void shouldFindById() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(testCourseIndicator));

        // When
        CourseIndicator found = courseIndicatorService.findById(1L);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
        verify(repository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenNotFound() {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> courseIndicatorService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("CourseIndicator not found with id: 999");
    }

    @Test
    void shouldSearchByCourseIdAndIndicatorIdAndIsActive() {
        // Given
        when(repository.findByCourseIdAndIndicatorIdAndIsActive(1L, 1L, true))
                .thenReturn(Optional.of(testCourseIndicator));

        // When
        List<CourseIndicator> results = courseIndicatorService.searchCourseIndicators(
                new CourseIndicatorSearchRequest(1, 1, true));

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCourseId()).isEqualTo(1L);
        verify(repository).findByCourseIdAndIndicatorIdAndIsActive(1L, 1L, true);
    }

    @Test
    void shouldSearchByCourseIdAndIsActive() {
        // Given
        when(repository.findByCourseIdAndIsActive(1L, true))
                .thenReturn(List.of(testCourseIndicator));

        // When
        List<CourseIndicator> results = courseIndicatorService.searchCourseIndicators(
                new CourseIndicatorSearchRequest(1, null, true));

        // Then
        assertThat(results).hasSize(1);
        verify(repository).findByCourseIdAndIsActive(1L, true);
    }

    @Test
    void shouldSearchByIndicatorIdAndIsActive() {
        // Given
        when(repository.findByIndicatorIdAndIsActive(1L, true))
                .thenReturn(List.of(testCourseIndicator));

        // When
        List<CourseIndicator> results = courseIndicatorService.searchCourseIndicators(
                new CourseIndicatorSearchRequest(null, 1, true));

        // Then
        assertThat(results).hasSize(1);
        verify(repository).findByIndicatorIdAndIsActive(1L, true);
    }

    @Test
    void shouldSearchByCourseIdOnly() {
        // Given
        when(repository.findByCourseId(1L)).thenReturn(List.of(testCourseIndicator));

        // When
        List<CourseIndicator> results = courseIndicatorService.searchCourseIndicators(
                new CourseIndicatorSearchRequest(1, null, null));

        // Then
        assertThat(results).hasSize(1);
        verify(repository).findByCourseId(1L);
    }

    @Test
    void shouldSearchByIndicatorIdOnly() {
        // Given
        when(repository.findByIndicatorId(1L)).thenReturn(List.of(testCourseIndicator));

        // When
        List<CourseIndicator> results = courseIndicatorService.searchCourseIndicators(
                new CourseIndicatorSearchRequest(null, 1, null));

        // Then
        assertThat(results).hasSize(1);
        verify(repository).findByIndicatorId(1L);
    }

    @Test
    void shouldSearchByIsActiveOnly() {
        // Given
        when(repository.findByIsActive(true)).thenReturn(List.of(testCourseIndicator));

        // When
        List<CourseIndicator> results = courseIndicatorService.searchCourseIndicators(
                new CourseIndicatorSearchRequest(null, null, true));

        // Then
        assertThat(results).hasSize(1);
        verify(repository).findByIsActive(true);
    }

    @Test
    void shouldFindActiveIndicatorsByCourseId() {
        // Given
        when(repository.findActiveIndicatorsByCourseId(1L)).thenReturn(List.of(testCourseIndicator));

        // When
        List<CourseIndicator> results = courseIndicatorService.findActiveIndicatorsByCourseId(1L);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getIsActive()).isTrue();
        verify(repository).findActiveIndicatorsByCourseId(1L);
    }

    @Test
    void shouldFindActiveCoursesByIndicatorId() {
        // Given
        when(repository.findActiveCoursesByIndicatorId(1L)).thenReturn(List.of(testCourseIndicator));

        // When
        List<CourseIndicator> results = courseIndicatorService.findActiveCoursesByIndicatorId(1L);

        // Then
        assertThat(results).hasSize(1);
        verify(repository).findActiveCoursesByIndicatorId(1L);
    }

    @Test
    void shouldActivateCourseIndicator() {
        // Given
        testCourseIndicator.setIsActive(false);
        when(repository.findById(1L)).thenReturn(Optional.of(testCourseIndicator));
        when(repository.save(any(CourseIndicator.class))).thenReturn(testCourseIndicator);

        // When
        CourseIndicator result = courseIndicatorService.activate(1L);

        // Then
        assertThat(result.getIsActive()).isTrue();
        verify(repository).findById(1L);
        verify(repository).save(testCourseIndicator);
    }

    @Test
    void shouldDeactivateCourseIndicator() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(testCourseIndicator));
        when(repository.save(any(CourseIndicator.class))).thenReturn(testCourseIndicator);

        // When
        CourseIndicator result = courseIndicatorService.deactivate(1L);

        // Then
        assertThat(result.getIsActive()).isFalse();
        verify(repository).findById(1L);
        verify(repository).save(testCourseIndicator);
    }

    @Test
    void shouldCheckHasActiveIndicators() {
        // Given
        when(repository.hasActiveIndicators(1L)).thenReturn(true);

        // When/Then
        assertThat(courseIndicatorService.hasActiveIndicators(1L)).isTrue();
        verify(repository).hasActiveIndicators(1L);
    }

    @Test
    void shouldCountActiveIndicatorsByCourse() {
        // Given
        when(repository.countByCourseIdAndIsActive(1L, true)).thenReturn(3L);

        // When
        long count = courseIndicatorService.countActiveIndicatorsByCourse(1L);

        // Then
        assertThat(count).isEqualTo(3L);
        verify(repository).countByCourseIdAndIsActive(1L, true);
    }
}