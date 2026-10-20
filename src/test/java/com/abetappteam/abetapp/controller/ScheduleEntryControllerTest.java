package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.BaseControllerTest;
import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.entity.Requests.ScheduleEntry.ScheduleEntrySearchRequest;
import com.abetappteam.abetapp.entity.ScheduleEntry;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.service.ScheduleEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScheduleEntryController.class)
@Import(TestSecurityConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
class ScheduleEntryControllerTest extends BaseControllerTest {

    @MockitoBean
    private ScheduleEntryService scheduleEntryService;

    private ScheduleEntry testScheduleEntry;

    @BeforeEach
    void setUp() {
        testScheduleEntry = new ScheduleEntry(1, 10, 2, 3);
        testScheduleEntry.setId(1L);
    }

    @Test
    void shouldSearchScheduleEntries() throws Exception {
        when(scheduleEntryService.searchScheduleEntry(any(ScheduleEntrySearchRequest.class)))
                .thenReturn(List.of(testScheduleEntry));

        mockMvc.perform(get("/api/schedule-entry")
                        .param("semesterId", "1")
                        .param("courseId", "10")
                        .param("programId", "2")
                        .param("indicatorId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ScheduleEntry results retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].semesterId").value(1))
                .andExpect(jsonPath("$.data[0].courseId").value(10))
                .andExpect(jsonPath("$.data[0].programId").value(2))
                .andExpect(jsonPath("$.data[0].indicatorId").value(3));

        verify(scheduleEntryService).searchScheduleEntry(argThat(request ->
                request.id() == null
                        && Integer.valueOf(1).equals(request.semesterId())
                        && Integer.valueOf(10).equals(request.courseId())
                        && Integer.valueOf(2).equals(request.programId())
                        && Integer.valueOf(3).equals(request.indicatorId())));
    }

    @Test
    void shouldSearchWithNoFilters() throws Exception {
        when(scheduleEntryService.searchScheduleEntry(any(ScheduleEntrySearchRequest.class)))
                .thenReturn(List.of(testScheduleEntry));

        mockMvc.perform(get("/api/schedule-entry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(scheduleEntryService).searchScheduleEntry(argThat(request ->
                request.id() == null
                        && request.semesterId() == null
                        && request.courseId() == null
                        && request.programId() == null
                        && request.indicatorId() == null));
    }

    @Test
    void shouldCreateScheduleEntry() throws Exception {
        when(scheduleEntryService.createScheduleEntry(1, 10, 2, 3))
                .thenReturn(testScheduleEntry);

        mockMvc.perform(post("/api/schedule-entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(testScheduleEntry)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("SectionUser created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.semesterId").value(1))
                .andExpect(jsonPath("$.data.courseId").value(10))
                .andExpect(jsonPath("$.data.programId").value(2))
                .andExpect(jsonPath("$.data.indicatorId").value(3));

        verify(scheduleEntryService).createScheduleEntry(1, 10, 2, 3);
    }

    @Test
    void shouldReturnErrorWhenDuplicateScheduleEntryExists() throws Exception {
        when(scheduleEntryService.createScheduleEntry(1, 10, 2, 3))
                .thenThrow(new IllegalArgumentException(
                        "This user is already assigned to the given section."));

        mockMvc.perform(post("/api/schedule-entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(testScheduleEntry)))
                .andExpect(status().isBadRequest());

        verify(scheduleEntryService).createScheduleEntry(1, 10, 2, 3);
    }

    @Test
    void shouldDeleteScheduleEntry() throws Exception {
        doNothing().when(scheduleEntryService).removeScheduleEntry(1L);

        mockMvc.perform(delete("/api/schedule-entry/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ScheduleEntry deleted successfully"));

        verify(scheduleEntryService).removeScheduleEntry(1L);
    }

    @Test
    void shouldRejectInvalidIdOnDelete() throws Exception {
        mockMvc.perform(delete("/api/schedule-entry/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid ID provided"));

        verify(scheduleEntryService, never()).removeScheduleEntry(anyLong());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingScheduleEntry() throws Exception {
        doThrow(new ResourceNotFoundException("ScheduleEntry not found with id: 999"))
                .when(scheduleEntryService).removeScheduleEntry(999L);

        mockMvc.perform(delete("/api/schedule-entry/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("ScheduleEntry not found with id: 999"));

        verify(scheduleEntryService).removeScheduleEntry(999L);
    }
}
