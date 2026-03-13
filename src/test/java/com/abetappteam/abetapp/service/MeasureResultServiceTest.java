package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.BaseServiceTest;
import com.abetappteam.abetapp.dto.MeasureResultDTO;
import com.abetappteam.abetapp.entity.MeasureResult;
import com.abetappteam.abetapp.entity.Requests.MeasureResults.MeasureResultsSearchRequest;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.repository.MeasureResultRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MeasureResultServiceTest extends BaseServiceTest {

    @Mock
    private MeasureResultRepository measureResultRepository;

    @InjectMocks
    private MeasureResultService measureResultService;

    private MeasureResult testMeasureResult;
    private MeasureResultDTO testDTO;

    @BeforeEach
    void setUp() {
        testMeasureResult = new MeasureResult(1L, 1L, 1L, 10, 5, 3, "Test Observation", null, "InProgress");
        testMeasureResult.setId(1L);

        testDTO = new MeasureResultDTO();
        testDTO.setMeasureId(1L);
        testDTO.setSectionId(1L);
        testDTO.setProgramId(1L);
        testDTO.setStudentsMet(10);
        testDTO.setStudentsExceeded(5);
        testDTO.setStudentsBelow(3);
        testDTO.setObservation("Test Observation");
        testDTO.setStatus("InProgress");
    }

    @Test
    void shouldFindById() {
        // Given
        when(measureResultRepository.findById(1L)).thenReturn(Optional.of(testMeasureResult));

        // When
        MeasureResult found = measureResultService.findById(1L);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getMeasureId()).isEqualTo(1L);
        assertThat(found.getStatus()).isEqualTo("InProgress");
        verify(measureResultRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenNotFound() {
        // Given
        when(measureResultRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> measureResultService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("MeasureResult not found with id: 999");
    }

    @Test
    void shouldCreateMeasureResult() {
        // Given
        when(measureResultRepository.save(any(MeasureResult.class))).thenReturn(testMeasureResult);

        // When
        MeasureResult created = measureResultService.create(testDTO);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getMeasureId()).isEqualTo(1L);
        verify(measureResultRepository).save(any(MeasureResult.class));
    }

    @Test
    void shouldUpdateMeasureResult() {
        // Given
        when(measureResultRepository.findById(1L)).thenReturn(Optional.of(testMeasureResult));
        when(measureResultRepository.save(any(MeasureResult.class))).thenReturn(testMeasureResult);

        // When
        MeasureResult updated = measureResultService.update(1L, testDTO);

        // Then
        assertThat(updated).isNotNull();
        verify(measureResultRepository).findById(1L);
        verify(measureResultRepository).save(any(MeasureResult.class));
    }

    @Test
    void shouldDeleteMeasureResult() {
        // Given
        when(measureResultRepository.findById(1L)).thenReturn(Optional.of(testMeasureResult));
        doNothing().when(measureResultRepository).delete(testMeasureResult);

        // When
        measureResultService.delete(1L);

        // Then
        verify(measureResultRepository).findById(1L);
        verify(measureResultRepository).delete(testMeasureResult);
    }

    @Test
    void shouldSearchMeasureResults() {
        // Given
        MeasureResultsSearchRequest request = new MeasureResultsSearchRequest(0, 1, 0, 0);
        List<MeasureResult> results = List.of(testMeasureResult);
        when(measureResultRepository.searchMeasureResults(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(results);

        // When
        List<MeasureResult> found = measureResultService.searchMeasureResults(request);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getMeasureId()).isEqualTo(1L);
        verify(measureResultRepository).searchMeasureResults(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void shouldCreateFromImport() {
        // Given
        when(measureResultRepository.save(any(MeasureResult.class))).thenReturn(testMeasureResult);

        // When
        MeasureResult saved = measureResultService.createFromImport(testMeasureResult);

        // Then
        assertThat(saved).isNotNull();
        verify(measureResultRepository).save(testMeasureResult);
    }
}