package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.BaseControllerTest;
import com.abetappteam.abetapp.dto.SemesterDTO;
import com.abetappteam.abetapp.entity.Semester;
import com.abetappteam.abetapp.entity.Semester.SemesterStatus;
import com.abetappteam.abetapp.entity.Semester.SemesterType;
import com.abetappteam.abetapp.entity.Requests.Semester.SemesterSearchRequest;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.service.SemesterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for SemesterController
 */
@WebMvcTest(SemesterController.class)
@Import(TestSecurityConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
class SemesterControllerUnitTest extends BaseControllerTest {

    @MockitoBean
    private SemesterService semesterService;

    private Semester testSemester;
    private SemesterDTO testSemesterDTO;

    @BeforeEach
    void setUp() {
        testSemester = new Semester();
        testSemester.setId(1L);
        testSemester.setName("Fall 2025");
        testSemester.setCode("FALL-2025");
        testSemester.setStartDate(LocalDate.of(2025, 8, 25));
        testSemester.setEndDate(LocalDate.of(2025, 12, 15));
        testSemester.setAcademicYear(2025);
        testSemester.setType(SemesterType.FALL);
        testSemester.setStatus(SemesterStatus.UPCOMING);
        testSemester.setProgramId(1L);
        testSemester.setDescription("Fall Semester 2025");
        testSemester.setIsCurrent(false);

        testSemesterDTO = new SemesterDTO("Fall 2025", "FALL-2025",
                LocalDate.of(2025, 8, 25),
                (LocalDate.of(2025, 12, 15)),
                2025, "FALL", 1L, "Fall Semester 2025", false);
    }

    // TODO: Come through and refactor these tests with updated search code
    @Test
    void shouldGetAllSemestersByProgram() throws Exception {
        // Given
        List<Semester> semesters = List.of(testSemester);
        when(semesterService.searchSemesters(any(SemesterSearchRequest.class))).thenReturn(semesters);

        // When/Then
        mockMvc.perform(get("/api/semesters")
                .param("programId", "1")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(semesterService, times(1)).searchSemesters(any(SemesterSearchRequest.class));
    }

    @Test
    void shouldGetSemesterById() throws Exception {
        // Given: search by id=1 should return the semester in a list
        Map<String, Object> body = new HashMap<>();
        body.put("id", 1);
        body.put("status", null);
        body.put("academicYear", null);
        body.put("startDate", null);
        body.put("endDate", null);
        body.put("type", null);
        body.put("name", null);
        body.put("code", null);

        when(semesterService.searchSemesters(any())).thenReturn(List.of(testSemester));

        // When/Then
        mockMvc.perform(get("/api/semesters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Fall 2025"));

        verify(semesterService, times(1)).searchSemesters(any());
    }

    @Test
    void shouldCreateSemester() throws Exception {
        // Given
        when(semesterService.createSemester(any(SemesterDTO.class))).thenReturn(testSemester);

        // When/Then
        mockMvc.perform(post("/api/semesters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(testSemesterDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(semesterService, times(1)).createSemester(any(SemesterDTO.class));
    }

    @Test
    void shouldReturnBadRequestForInvalidSemester() throws Exception {
        // Given - DTO with missing required fields
        SemesterDTO invalidDTO = new SemesterDTO(null, null, null, null, null, null, null, null, null);
        invalidDTO.setName(null); // Invalid - name is required
        invalidDTO.setCode(null); // Invalid - code is required

        // When/Then
        mockMvc.perform(post("/api/semesters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(semesterService, never()).createSemester(any(SemesterDTO.class));
    }

    @Test
    void shouldUpdateSemester() throws Exception {
        // Given
        when(semesterService.updateSemester(eq(1L), any(SemesterDTO.class))).thenReturn(testSemester);

        // When/Then
        mockMvc.perform(put("/api/semesters/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(testSemesterDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Semester updated successfully"));

        verify(semesterService, times(1)).updateSemester(eq(1L), any(SemesterDTO.class));
    }

    @Test
    void shouldRemoveSemester() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/semesters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Semester removed successfully"));

        verify(semesterService, times(1)).removeSemester(1L);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingSemester() throws Exception {
        when(semesterService.updateSemester(eq(999L), any(SemesterDTO.class)))
                .thenThrow(new ResourceNotFoundException("Semester not found with id: 999"));

        mockMvc.perform(put("/api/semesters/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(testSemesterDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        verify(semesterService).updateSemester(eq(999L), any(SemesterDTO.class));
    }

    @Test
    void shouldSetAsCurrentSemester() throws Exception {
        // Given
        when(semesterService.setAsCurrentSemester(1L)).thenReturn(testSemester);

        // When/Then
        mockMvc.perform(post("/api/semesters/1/set-current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Semester set as current successfully"));

        verify(semesterService, times(1)).setAsCurrentSemester(1L);
    }

    @Test
    void shouldUpdateSemesterStatus() throws Exception {
        // Given
        when(semesterService.updateSemesterStatus(eq(1L), eq(SemesterStatus.ACTIVE))).thenReturn(testSemester);

        // When/Then
        mockMvc.perform(put("/api/semesters/1/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Semester status updated successfully"));

        verify(semesterService, times(1)).updateSemesterStatus(eq(1L), eq(SemesterStatus.ACTIVE));
    }

    @Test
    void shouldReturnBadRequestForInvalidSemesterId() throws Exception {
        // Searching with id=0 should return no results (controller accepts search body)
        Map<String, Object> body = new HashMap<>();
        body.put("id", 0);
        body.put("status", null);
        body.put("academicYear", null);
        body.put("startDate", null);
        body.put("endDate", null);
        body.put("type", null);
        body.put("name", null);
        body.put("code", null);

        when(semesterService.searchSemesters(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/semesters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(semesterService, times(1)).searchSemesters(any());
    }

    @Test
    void shouldCountByProgramAndStatus() throws Exception {
        // Use the search endpoint to fetch semesters with status=ACTIVE and assert we
        // get 3 results
        var s2 = new Semester();
        s2.setId(2L);
        s2.setName("Spring 2025");
        s2.setType(SemesterType.FALL);

        var s3 = new Semester();
        s3.setId(3L);
        s3.setName("Summer 2025");
        s3.setType(SemesterType.FALL);

        when(semesterService.searchSemesters(any())).thenReturn(List.of(testSemester, s2, s3));

        Map<String, Object> body = new HashMap<>();
        body.put("id", null);
        body.put("status", "ACTIVE");
        body.put("academicYear", null);
        body.put("startDate", null);
        body.put("endDate", null);
        body.put("type", null);
        body.put("name", null);
        body.put("code", null);

        mockMvc.perform(get("/api/semesters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));

        verify(semesterService, times(1)).searchSemesters(any());
    }

    @Test
    void shouldUpdateAllSemesterStatuses() throws Exception {
        mockMvc.perform(post("/api/semesters/update-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("All semester statuses updated successfully"));

        verify(semesterService).updateAllSemesterStatuses();
    }

    @Test
    void shouldClearCurrentSemesterFlag() throws Exception {
        mockMvc.perform(delete("/api/semesters/program/1/clear-current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Current semester flag cleared successfully"));

        verify(semesterService).clearCurrentSemesterFlag(1L);
    }

    @Test
    void shouldGetSemestersByStatus() throws Exception {
        // Prepare search body for status UPCOMING
        Map<String, Object> body = new HashMap<>();
        body.put("id", null);
        body.put("status", "UPCOMING");
        body.put("academicYear", null);
        body.put("startDate", null);
        body.put("endDate", null);
        body.put("type", null);
        body.put("name", null);
        body.put("code", null);

        when(semesterService.searchSemesters(any())).thenReturn(List.of(testSemester));

        mockMvc.perform(get("/api/semesters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(semesterService).searchSemesters(any());
    }

    @Test
    void shouldGetAllSemesters() throws Exception {
        when(semesterService.searchSemesters(any(SemesterSearchRequest.class)))
                .thenReturn(List.of(testSemester));

        mockMvc.perform(get("/api/semesters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].code").value("FALL-2025"));

        verify(semesterService).searchSemesters(any(SemesterSearchRequest.class));
    }
}