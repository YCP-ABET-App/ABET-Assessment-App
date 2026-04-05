package com.abetappteam.abetapp.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.abetappteam.abetapp.BaseRepositoryTest;
import com.abetappteam.abetapp.entity.Measure;
import com.abetappteam.abetapp.util.TestDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.List;

public class MeasureRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private MeasureRepository measureRepository;

    private Measure testMeasure;

    @BeforeEach
    void setUp() {
        testMeasure = TestDataBuilder.createMeasure();
    }

    @Test
    void shouldSaveAndRetrieveMeasure() {
        // Given
        Measure saved = measureRepository.save(testMeasure);
        clearContext();

        // When
        Optional<Measure> found = measureRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCourseIndicatorId()).isEqualTo(1l);
        assertThat(found.get().getDescription()).isEqualTo("Example Description");
        assertThat(found.get().getActive()).isEqualTo(true);
    }

    @Test
    void shouldUpdateMeasure() {
        // Given
        Measure saved = measureRepository.save(testMeasure);
        entityManager.flush();
        entityManager.clear();

        // When
        Measure toUpdate = measureRepository.findById(saved.getId()).orElseThrow();
        toUpdate.setDescription("New Description");
        measureRepository.save(toUpdate);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Measure> found = measureRepository.findById(toUpdate.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("New Description");
    }

    @Test
    void shouldDeleteMeasure() {
        // Given
        Measure saved = measureRepository.save(testMeasure);
        entityManager.flush();
        Long id = saved.getId();
        entityManager.clear();

        // When
        measureRepository.deleteById(id);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(measureRepository.findById(id)).isEmpty();
    }

    @Test
    void shouldSearchById() {
        Measure saved = measureRepository.save(testMeasure);
        measureRepository.save(TestDataBuilder.createMeasure(1L, 1L, 1L, "Other Measure", "InProgress", true));

        List<Measure> found = measureRepository.searchMeasures(saved.getId().intValue(), null, null, null);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(saved.getId());
    }

    @Test
    void shouldSearchByCourseIndicatorId() {
        measureRepository.save(testMeasure);
        measureRepository.save(TestDataBuilder.createMeasure(2L, 2L, 2L, "Other Measure", "InProgress", true));

        List<Measure> found = measureRepository.searchMeasures(null, 1, null, null);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getCourseIndicatorId()).isEqualTo(1L);
    }

    @Test
    void shouldSearchByActiveStatus() {
        measureRepository.save(testMeasure);
        measureRepository.save(TestDataBuilder.createMeasure(1L, 1L, 1L, "Inactive", "InProgress", false));

        List<Measure> found = measureRepository.searchMeasures(null, null, null, true);

        assertThat(found).hasSize(1);
        assertThat(found).allMatch(Measure::getActive);
    }

    @Test
    void shouldReturnAllWhenNoFilters() {
        measureRepository.save(testMeasure);
        measureRepository.save(TestDataBuilder.createMeasure(1L, 1L, 1L, "Another Measure", "Submitted", true));

        List<Measure> found = measureRepository.searchMeasures(null, null, null, null);

        assertThat(found).hasSize(2);
    }
}
