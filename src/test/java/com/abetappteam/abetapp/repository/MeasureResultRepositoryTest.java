package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.BaseRepositoryTest;
import com.abetappteam.abetapp.entity.MeasureResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MeasureResultRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private MeasureResultRepository measureResultRepository;

    private MeasureResult testMeasureResult;

    @BeforeEach
    void setUp() {
        testMeasureResult = new MeasureResult(1L, 1L, 10, 5, 3, "Test Observation", null, "InProgress");
    }

    @Test
    void shouldSaveAndRetrieveMeasureResult() {
        // Given
        MeasureResult saved = measureResultRepository.save(testMeasureResult);
        flushAndClear();

        // When
        Optional<MeasureResult> found = measureResultRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getMeasureId()).isEqualTo(1L);
        assertThat(found.get().getStatus()).isEqualTo("InProgress");
    }

    @Test
    void shouldFindByMeasureId() {
        // Given
        measureResultRepository.save(testMeasureResult);
        flushAndClear();

        // When
        List<MeasureResult> found = measureResultRepository.findByMeasureId(1L);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found).extracting(MeasureResult::getMeasureId).containsExactly(1L);
    }

    @Test
    void shouldFindByMeasureIdAndStatus() {
        // Given
        measureResultRepository.save(testMeasureResult);
        flushAndClear();

        // When
        List<MeasureResult> found = measureResultRepository.findByMeasureIdAndStatus(1L, "InProgress");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found).extracting(MeasureResult::getStatus).containsExactly("InProgress");
    }

    @Test
    void shouldFindByMeasureIdAndSectionProgramId() {
        // Given
        measureResultRepository.save(testMeasureResult);
        flushAndClear();

        // When
        Optional<MeasureResult> found = measureResultRepository.findByMeasureIdAndSectionProgramId(1L, 1L);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getSectionProgramId()).isEqualTo(1L);
    }

    @Test
    void shouldCheckExistsByMeasureIdAndSectionProgramId() {
        // Given
        measureResultRepository.save(testMeasureResult);
        flushAndClear();

        // When/Then
        assertThat(measureResultRepository.existsByMeasureIdAndSectionProgramId(1L, 1L)).isTrue();
        assertThat(measureResultRepository.existsByMeasureIdAndSectionProgramId(1L, 999L)).isFalse();
    }

    @Test
    void shouldCountByMeasureId() {
        // Given
        MeasureResult second = new MeasureResult(1L, 2L, 8, 2, 1, "Another", null, "Submitted");
        measureResultRepository.save(testMeasureResult);
        measureResultRepository.save(second);
        flushAndClear();

        // When
        long count = measureResultRepository.countByMeasureId(1L);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldSearchMeasureResults() {
        // Given
        MeasureResult saved = measureResultRepository.save(testMeasureResult);
        flushAndClear();

        // When
        List<MeasureResult> found = measureResultRepository.searchMeasureResults(saved.getId().intValue(), 1,  1);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found).extracting(MeasureResult::getMeasureId).containsExactly(1L);
    }

    @Test
    void shouldCheckHasActiveSections() {
        // Given
        measureResultRepository.save(testMeasureResult);
        flushAndClear();

        // When/Then
        assertThat(measureResultRepository.hasActiveSectionPrograms(1L)).isTrue();

        MeasureResult rejected = new MeasureResult(1L, 2L, 0, 0, 0, null, "Not valid", "Rejected");
        measureResultRepository.save(rejected);
        flushAndClear();

        assertThat(measureResultRepository.hasActiveSectionPrograms(2L)).isFalse();
    }

    @Test
    void shouldCheckHasActivePrograms() {
        // Given
        measureResultRepository.save(testMeasureResult);
        flushAndClear();

        // When/Then
        assertThat(measureResultRepository.hasActiveSectionPrograms(1L)).isTrue();
    }

    @Test
    void shouldDeleteByMeasureId() {
        // Given
        measureResultRepository.save(testMeasureResult);
        flushAndClear();

        // When
        measureResultRepository.deleteByMeasureId(1L);
        flushAndClear();

        // Then
        assertThat(measureResultRepository.findByMeasureId(1L)).isEmpty();
    }
}