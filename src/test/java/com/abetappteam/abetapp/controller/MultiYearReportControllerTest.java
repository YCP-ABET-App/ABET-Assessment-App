package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.BaseControllerTest;
import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.dto.report.MultiYearReportData;
import com.abetappteam.abetapp.dto.report.OutcomeReportData;
import com.abetappteam.abetapp.exception.BusinessException;
import com.abetappteam.abetapp.service.MultiYearReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MultiYearReportController.class)
@Import(TestSecurityConfig.class)
class MultiYearReportControllerTest extends BaseControllerTest {

    @MockitoBean
    private MultiYearReportService multiYearReportService;

    private MultiYearReportData singleReport;
    private List<MultiYearReportData> multiYearReports;

    @BeforeEach
    void setUp() {
        singleReport = new MultiYearReportData(10L, "Fall 2023", "2023–2024", "9/1/2023");
        OutcomeReportData outcome = new OutcomeReportData(50L, 1, "Student Outcome 1", "MET");
        singleReport.setOutcomes(List.of(outcome));

        MultiYearReportData year2023 = new MultiYearReportData(10L, "Fall 2023", "2023–2024", "9/1/2023");
        MultiYearReportData year2024 = new MultiYearReportData(20L, "Fall 2024", "2024–2025", "9/1/2024");
        multiYearReports = List.of(year2023, year2024);
    }

    // ─── GET /api/reports/summary ─────────────────────────────────────────────────

    @Test
    void getSummaryReport_returnsReportSuccessfully() throws Exception {
        when(multiYearReportService.buildReportForSemester(1L, 10L)).thenReturn(singleReport);

        mockMvc.perform(get("/api/reports/summary")
                        .param("programId", "1")
                        .param("semesterId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.semesterId").value(10))
                .andExpect(jsonPath("$.data.academicYear").value("2023–2024"))
                .andExpect(jsonPath("$.data.outcomes[0].outcomeId").value(50));
    }

    @Test
    void getSummaryReport_returnsErrorWhenProgramIdMissing() throws Exception {
        mockMvc.perform(get("/api/reports/summary")
                        .param("semesterId", "10"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getSummaryReport_returnsErrorWhenSemesterIdMissing() throws Exception {
        mockMvc.perform(get("/api/reports/summary")
                        .param("programId", "1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getSummaryReport_returns4xxWhenServiceThrowsBusinessException() throws Exception {
        when(multiYearReportService.buildReportForSemester(1L, 10L))
                .thenThrow(new BusinessException("Semester not found: 10"));

        mockMvc.perform(get("/api/reports/summary")
                        .param("programId", "1")
                        .param("semesterId", "10"))
                .andExpect(status().is4xxClientError());
    }

    // ─── GET /api/reports/multi-year ──────────────────────────────────────────────

    @Test
    void getMultiYearReport_returnsReportListSuccessfully() throws Exception {
        when(multiYearReportService.buildReportByAcademicYear(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(multiYearReports);

        mockMvc.perform(get("/api/reports/multi-year")
                        .param("programId", "1")
                        .param("startDate", "2023-09-01")
                        .param("endDate", "2025-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].academicYear").value("2023–2024"))
                .andExpect(jsonPath("$.data[1].academicYear").value("2024–2025"));
    }

    @Test
    void getMultiYearReport_returnsErrorWhenStartDateMissing() throws Exception {
        mockMvc.perform(get("/api/reports/multi-year")
                        .param("programId", "1")
                        .param("endDate", "2025-05-31"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getMultiYearReport_returnsErrorWhenEndDateMissing() throws Exception {
        mockMvc.perform(get("/api/reports/multi-year")
                        .param("programId", "1")
                        .param("startDate", "2023-09-01"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getMultiYearReport_returns4xxWhenServiceThrowsBusinessException() throws Exception {
        when(multiYearReportService.buildReportByAcademicYear(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new BusinessException("No semesters found in the selected date range"));

        mockMvc.perform(get("/api/reports/multi-year")
                        .param("programId", "1")
                        .param("startDate", "2023-09-01")
                        .param("endDate", "2025-05-31"))
                .andExpect(status().is4xxClientError());
    }
}
