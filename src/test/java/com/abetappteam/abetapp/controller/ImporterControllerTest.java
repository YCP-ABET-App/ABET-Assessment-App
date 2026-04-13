package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.dto.importer.*;
import com.abetappteam.abetapp.service.ImporterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImporterController.class)
@Import(TestSecurityConfig.class)
class ImporterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ImporterService importerService;

    private SummaryImportDTO testSummaryDTO;

    @BeforeEach
    void setUp() {
        testSummaryDTO = createTestSummaryDTO();
    }

    @Test
    void shouldImportSummarySuccessfully() throws Exception {
        // Given
        doNothing().when(importerService).importSummary(any(SummaryImportDTO.class), eq(1L));

        // When/Then
        mockMvc.perform(post("/api/import/summary")

                .param("programId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSummaryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data").value("Summary imported successfully"));

        verify(importerService, times(1)).importSummary(any(SummaryImportDTO.class), eq(1L));
    }

    private SummaryImportDTO createTestSummaryDTO() {
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
        measure.setDescription("Test measure");
        measure.setMetPercentage(75.0);
        measure.setRecommendedActions(Arrays.asList("Review material"));

        course.setMeasures(Arrays.asList(measure));
        indicator.setCourses(Arrays.asList(course));
        outcome.setIndicators(Arrays.asList(indicator));
        dto.setOutcomes(Arrays.asList(outcome));

        return dto;
    }
}