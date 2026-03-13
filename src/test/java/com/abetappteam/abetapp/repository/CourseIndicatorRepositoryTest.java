package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.BaseRepositoryTest;
import com.abetappteam.abetapp.entity.CourseIndicator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CourseIndicatorRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CourseIndicatorRepository courseIndicatorRepository;

    private CourseIndicator testCourseIndicator;

    @BeforeEach
    void setUp() {
        testCourseIndicator = new CourseIndicator(1L, 1L);
    }

    @Test
    void shouldSaveAndRetrieveCourseIndicator() {
        // Given
        CourseIndicator saved = courseIndicatorRepository.save(testCourseIndicator);
        flushAndClear();

        // When
        Optional<CourseIndicator> found = courseIndicatorRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCourseId()).isEqualTo(1L);
        assertThat(found.get().getIndicatorId()).isEqualTo(1L);
        assertThat(found.get().getIsActive()).isTrue();
    }

    @Test
    void shouldFindByCourseId() {
        // Given
        courseIndicatorRepository.save(testCourseIndicator);
        flushAndClear();

        // When
        List<CourseIndicator> found = courseIndicatorRepository.findByCourseId(1L);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found).extracting(CourseIndicator::getCourseId).containsExactly(1L);
    }

    @Test
    void shouldFindByCourseIdAndIsActive() {
        // Given
        courseIndicatorRepository.save(testCourseIndicator);
        CourseIndicator inactive = new CourseIndicator(1L, 2L);
        inactive.setIsActive(false);
        courseIndicatorRepository.save(inactive);
        flushAndClear();

        // When
        List<CourseIndicator> active = courseIndicatorRepository.findByCourseIdAndIsActive(1L, true);
        List<CourseIndicator> inactiveList = courseIndicatorRepository.findByCourseIdAndIsActive(1L, false);

        // Then
        assertThat(active).hasSize(1);
        assertThat(active).extracting(CourseIndicator::getIsActive).containsExactly(true);
        assertThat(inactiveList).hasSize(1);
        assertThat(inactiveList).extracting(CourseIndicator::getIsActive).containsExactly(false);
    }

    @Test
    void shouldFindByIndicatorId() {
        // Given
        courseIndicatorRepository.save(testCourseIndicator);
        flushAndClear();

        // When
        List<CourseIndicator> found = courseIndicatorRepository.findByIndicatorId(1L);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found).extracting(CourseIndicator::getIndicatorId).containsExactly(1L);
    }

    @Test
    void shouldFindByCourseIdAndIndicatorId() {
        // Given
        courseIndicatorRepository.save(testCourseIndicator);
        flushAndClear();

        // When
        Optional<CourseIndicator> found = courseIndicatorRepository.findByCourseIdAndIndicatorId(1L, 1L);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCourseId()).isEqualTo(1L);
        assertThat(found.get().getIndicatorId()).isEqualTo(1L);
    }

    @Test
    void shouldCheckExistsByCourseIdAndIndicatorId() {
        // Given
        courseIndicatorRepository.save(testCourseIndicator);
        flushAndClear();

        // When/Then
        assertThat(courseIndicatorRepository.existsByCourseIdAndIndicatorId(1L, 1L)).isTrue();
        assertThat(courseIndicatorRepository.existsByCourseIdAndIndicatorId(1L, 999L)).isFalse();
    }

    @Test
    void shouldFindActiveIndicatorsByCourseId() {
        // Given
        courseIndicatorRepository.save(testCourseIndicator);
        CourseIndicator inactive = new CourseIndicator(1L, 2L);
        inactive.setIsActive(false);
        courseIndicatorRepository.save(inactive);
        flushAndClear();

        // When
        List<CourseIndicator> found = courseIndicatorRepository.findActiveIndicatorsByCourseId(1L);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found).extracting(CourseIndicator::getIsActive).containsExactly(true);
    }

    @Test
    void shouldFindActiveCoursesByIndicatorId() {
        // Given
        courseIndicatorRepository.save(testCourseIndicator);
        flushAndClear();

        // When
        List<CourseIndicator> found = courseIndicatorRepository.findActiveCoursesByIndicatorId(1L);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found).extracting(CourseIndicator::getIndicatorId).containsExactly(1L);
    }

    @Test
    void shouldCheckHasActiveIndicators() {
        // Given
        courseIndicatorRepository.save(testCourseIndicator);
        flushAndClear();

        // When/Then
        assertThat(courseIndicatorRepository.hasActiveIndicators(1L)).isTrue();
        assertThat(courseIndicatorRepository.hasActiveIndicators(999L)).isFalse();
    }

    @Test
    void shouldCountByCourseIdAndIsActive() {
        // Given
        courseIndicatorRepository.save(testCourseIndicator);
        courseIndicatorRepository.save(new CourseIndicator(1L, 2L));
        CourseIndicator inactive = new CourseIndicator(1L, 3L);
        inactive.setIsActive(false);
        courseIndicatorRepository.save(inactive);
        flushAndClear();

        // When/Then
        assertThat(courseIndicatorRepository.countByCourseIdAndIsActive(1L, true)).isEqualTo(2);
        assertThat(courseIndicatorRepository.countByCourseIdAndIsActive(1L, false)).isEqualTo(1);
    }

    @Test
    void shouldDeleteByCourseIdAndIndicatorId() {
        // Given
        courseIndicatorRepository.save(testCourseIndicator);
        flushAndClear();

        // When
        courseIndicatorRepository.deleteByCourseIdAndIndicatorId(1L, 1L);
        flushAndClear();

        // Then
        assertThat(courseIndicatorRepository.findByCourseIdAndIndicatorId(1L, 1L)).isEmpty();
    }
}