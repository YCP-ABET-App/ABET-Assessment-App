package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.BaseControllerTest;
import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.entity.Requests.SectionIndicator.SectionIndicatorRequest;
import com.abetappteam.abetapp.entity.SectionIndicator;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.service.SectionIndicatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SectionIndicatorController.class)
@Import(TestSecurityConfig.class)
class SectionIndicatorControllerTest extends BaseControllerTest {

    @MockitoBean
    private SectionIndicatorService sectionIndicatorService;

    private SectionIndicator testSI;

    @BeforeEach
    void setUp() {
        testSI = new SectionIndicator(5, 10, false);
        testSI.setId(1L);
    }

    // ─── GET /api/section-indicator ───────────────────────────────────────────────

    @Test
    void searchSectionIndicators_returnsListSuccessfully() throws Exception {
        when(sectionIndicatorService.searchSectionIndicators(any(SectionIndicatorRequest.class)))
                .thenReturn(List.of(testSI));

        mockMvc.perform(get("/api/section-indicator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].sectionId").value(5))
                .andExpect(jsonPath("$.data[0].indicatorId").value(10));
    }

    @Test
    void searchSectionIndicators_acceptsSectionIdsFilterParam() throws Exception {
        when(sectionIndicatorService.searchSectionIndicators(any(SectionIndicatorRequest.class)))
                .thenReturn(List.of(testSI));

        mockMvc.perform(get("/api/section-indicator")
                        .param("sectionIds", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void searchSectionIndicators_returnsEmptyListWhenNoMatches() throws Exception {
        when(sectionIndicatorService.searchSectionIndicators(any(SectionIndicatorRequest.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/section-indicator")
                        .param("indicatorIds", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ─── GET /api/section-indicator/{id} ─────────────────────────────────────────

    @Test
    void getSectionIndicator_returnsEntryWhenFound() throws Exception {
        when(sectionIndicatorService.findById(1L)).thenReturn(testSI);

        mockMvc.perform(get("/api/section-indicator/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sectionId").value(5))
                .andExpect(jsonPath("$.data.indicatorId").value(10));
    }

    @Test
    void getSectionIndicator_returns404WhenNotFound() throws Exception {
        when(sectionIndicatorService.findById(99L))
                .thenThrow(new ResourceNotFoundException("SectionIndicator not found with id: 99"));

        mockMvc.perform(get("/api/section-indicator/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSectionIndicator_returnsBadRequestForInvalidId() throws Exception {
        mockMvc.perform(get("/api/section-indicator/0"))
                .andExpect(status().isBadRequest());
    }

    // ─── POST /api/section-indicator ──────────────────────────────────────────────

    @Test
    void createSectionIndicator_returnsCreatedEntry() throws Exception {
        when(sectionIndicatorService.createSectionIndicator(5, 10)).thenReturn(testSI);

        mockMvc.perform(post("/api/section-indicator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSI)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sectionId").value(5))
                .andExpect(jsonPath("$.data.indicatorId").value(10));

        verify(sectionIndicatorService).createSectionIndicator(5, 10);
    }

    // ─── PUT /api/section-indicator/{id} ─────────────────────────────────────────

    @Test
    void updateSectionIndicator_returnsUpdatedEntry() throws Exception {
        SectionIndicator updated = new SectionIndicator(99, 88, false);
        updated.setId(1L);
        when(sectionIndicatorService.updateSectionIndicator(eq(1L), eq(99), eq(88)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/section-indicator/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sectionId").value(99))
                .andExpect(jsonPath("$.data.indicatorId").value(88));
    }

    @Test
    void updateSectionIndicator_returns404WhenNotFound() throws Exception {
        when(sectionIndicatorService.updateSectionIndicator(eq(99L), any(), any()))
                .thenThrow(new ResourceNotFoundException("SectionIndicator not found with id: 99"));

        mockMvc.perform(put("/api/section-indicator/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSI)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSectionIndicator_returnsBadRequestForInvalidId() throws Exception {
        mockMvc.perform(put("/api/section-indicator/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSI)))
                .andExpect(status().isBadRequest());
    }

    // ─── DELETE /api/section-indicator/{id} ───────────────────────────────────────

    @Test
    void deleteSectionIndicator_returnsSuccessWhenDeleted() throws Exception {
        doNothing().when(sectionIndicatorService).removeSectionIndicator(1L);

        mockMvc.perform(delete("/api/section-indicator/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("SectionIndicator deleted successfully"));

        verify(sectionIndicatorService).removeSectionIndicator(1L);
    }

    @Test
    void deleteSectionIndicator_returns404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("SectionIndicator not found with id: 99"))
                .when(sectionIndicatorService).removeSectionIndicator(99L);

        mockMvc.perform(delete("/api/section-indicator/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSectionIndicator_returnsBadRequestForInvalidId() throws Exception {
        mockMvc.perform(delete("/api/section-indicator/0"))
                .andExpect(status().isBadRequest());
    }
}
