package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.dto.MeasureResultDTO;
import com.abetappteam.abetapp.entity.MeasureResult;
import com.abetappteam.abetapp.entity.Requests.MeasureResults.MeasureResultsSearchRequest;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.service.MeasureResultService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeasureResultController.class)
@Import(TestSecurityConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
public class MeasureResultControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MeasureResultService service;

    private MeasureResult testMeasureResult;
    private MeasureResultDTO testDTO;

    @BeforeEach
    void setUp() {
        testMeasureResult = new MeasureResult();
        testMeasureResult.setId(1L);
        testMeasureResult.setMeasureId(1L);
        testMeasureResult.setSectionProgramId(1L);
        testMeasureResult.setStudentsMet(10);
        testMeasureResult.setStudentsExceeded(5);
        testMeasureResult.setStudentsBelow(3);
        testMeasureResult.setObservation("Test Observation");
        testMeasureResult.setStatus("InProgress");

        testDTO = new MeasureResultDTO(1L, 1L,  10, 5, 3, "Test Observation", "InProgress", null);

    }

    @Test
    void shouldSearchMeasureResultsWithFilters() throws Exception {
        // Given
        List<MeasureResult> results = List.of(testMeasureResult);
        when(service.searchMeasureResults(any(MeasureResultsSearchRequest.class))).thenReturn(results);

        // When/Then
        mockMvc.perform(get("/api/measure-result")
                .param("id", "1")
                .param("measureId", "1")
                .param("sectionId", "1")
                .param("programId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Measure results retrieved successfully"))
                .andExpect(jsonPath("$.data[0].measureId").value(1));

        verify(service, times(1)).searchMeasureResults(any(MeasureResultsSearchRequest.class));
    }

    @Test
    void shouldSearchMeasureResultsWithNoFilters() throws Exception {
        // Given
        List<MeasureResult> results = List.of(testMeasureResult);
        when(service.searchMeasureResults(any(MeasureResultsSearchRequest.class))).thenReturn(results);

        // When/Then - all params are optional; omitting them hits the default/null path
        mockMvc.perform(get("/api/measure-result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].measureId").value(1));

        verify(service, times(1)).searchMeasureResults(any(MeasureResultsSearchRequest.class));
    }

    @Test
    void shouldCreateMeasureResult() throws Exception {
        // Given
        when(service.create(any(MeasureResultDTO.class))).thenReturn(testMeasureResult);

        // When/Then
        mockMvc.perform(post("/api/measure-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.measureId").value(1))
                .andExpect(jsonPath("$.data.status").value("InProgress"));

        verify(service, times(1)).create(any(MeasureResultDTO.class));
    }

    @Test
    void shouldUpdateMeasureResult() throws Exception {
        // Given
        when(service.update(eq(1L), any(MeasureResultDTO.class))).thenReturn(testMeasureResult);

        // When/Then
        mockMvc.perform(put("/api/measure-result/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Measure result updated successfully"))
                .andExpect(jsonPath("$.data.measureId").value(1));

        verify(service, times(1)).update(eq(1L), any(MeasureResultDTO.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentMeasureResult() throws Exception {
        // Given
        when(service.update(eq(999L), any(MeasureResultDTO.class)))
                .thenThrow(new ResourceNotFoundException("MeasureResult not found with id: 999"));

        // When/Then
        mockMvc.perform(put("/api/measure-result/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("MeasureResult not found with id: 999"));

        verify(service, times(1)).update(eq(999L), any(MeasureResultDTO.class));
    }

    @Test
    void shouldDeleteMeasureResult() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/measure-result/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Measure result deleted successfully"));

        verify(service, times(1)).delete(1L);
    }
    


}