package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.entity.CourseIndicator;
import com.abetappteam.abetapp.repository.CourseIndicatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
}