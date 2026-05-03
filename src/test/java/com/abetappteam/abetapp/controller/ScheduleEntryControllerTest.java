package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.BaseControllerTest;
import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.entity.Requests.ScheduleEntry.ScheduleEntrySearchRequest;
import com.abetappteam.abetapp.entity.ScheduleEntry;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.service.ScheduleEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScheduleEntryController.class)
@Import(TestSecurityConfig.class)
class ScheduleEntryControllerTest extends BaseControllerTest {

    @MockitoBean
    private ScheduleEntryService scheduleEntryService;

    private ScheduleEntry testEntry;

    @BeforeEach
    void setUp() {
        testEntry = new ScheduleEntry(1, 2, 3, 4);
        testEntry.setId(10L);
    }

    // ─── GET /api/schedule-entry ──────────────────────────────────────────────────

    @Test
    void searchScheduleEntry_returnsListSuccessfully() throws Exception {
        when(scheduleEntryService.searchScheduleEntry(any(ScheduleEntrySearchRequest.class)))
                .thenReturn(List.of(testEntry));

        mockMvc.perform(get("/api/schedule-entry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].semesterId").value(1))
                .andExpect(jsonPath("$.data[0].courseId").value(2));
    }

    @Test
    void searchScheduleEntry_acceptsOptionalFilterParams() throws Exception {
        ScheduleEntry entry1 = new ScheduleEntry(1, 2, 3, 4);
        entry1.setId(10L);
        ScheduleEntry entry2 = new ScheduleEntry(1, 5, 3, 4);
        entry2.setId(11L);
        when(scheduleEntryService.searchScheduleEntry(any(ScheduleEntrySearchRequest.class)))
                .thenReturn(List.of(entry1, entry2));

        mockMvc.perform(get("/api/schedule-entry")
                        .param("semesterId", "1")
                        .param("programId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void searchScheduleEntry_returnsEmptyListWhenNoResults() throws Exception {
        when(scheduleEntryService.searchScheduleEntry(any(ScheduleEntrySearchRequest.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/schedule-entry")
                        .param("semesterId", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ─── POST /api/schedule-entry ─────────────────────────────────────────────────

    @Test
    void createScheduleEntry_returnsCreatedEntry() throws Exception {
        when(scheduleEntryService.createScheduleEntry(1, 2, 3, 4)).thenReturn(testEntry);

        String body = objectMapper.writeValueAsString(testEntry);

        mockMvc.perform(post("/api/schedule-entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.semesterId").value(1))
                .andExpect(jsonPath("$.data.courseId").value(2))
                .andExpect(jsonPath("$.data.programId").value(3))
                .andExpect(jsonPath("$.data.indicatorId").value(4));
    }

    @Test
    void createScheduleEntry_returnsBadRequestWhenDuplicateExists() throws Exception {
        when(scheduleEntryService.createScheduleEntry(1, 2, 3, 4))
                .thenThrow(new IllegalArgumentException("This user is already assigned to the given section."));

        String body = objectMapper.writeValueAsString(testEntry);

        mockMvc.perform(post("/api/schedule-entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ─── DELETE /api/schedule-entry/{id} ─────────────────────────────────────────

    @Test
    void deleteScheduleEntry_returnsSuccessWhenDeleted() throws Exception {
        doNothing().when(scheduleEntryService).removeScheduleEntry(10L);

        mockMvc.perform(delete("/api/schedule-entry/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ScheduleEntry deleted successfully"));

        verify(scheduleEntryService).removeScheduleEntry(10L);
    }

    @Test
    void deleteScheduleEntry_returns404WhenEntryNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("ScheduleEntry not found with id: 99"))
                .when(scheduleEntryService).removeScheduleEntry(99L);

        mockMvc.perform(delete("/api/schedule-entry/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteScheduleEntry_returnsBadRequestForInvalidId() throws Exception {
        mockMvc.perform(delete("/api/schedule-entry/0"))
                .andExpect(status().isBadRequest());
    }
}
