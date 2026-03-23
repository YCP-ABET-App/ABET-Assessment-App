package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.BaseControllerTest;
import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.entity.Requests.SectionProgram.SectionProgramSearchRequest;
import com.abetappteam.abetapp.entity.SectionProgram;
import com.abetappteam.abetapp.service.SectionProgramService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SectionProgramController.class)
@Import(TestSecurityConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
class SectionProgramControllerUnitTest extends BaseControllerTest {

    @MockitoBean
    private SectionProgramService sectionProgramService;

    private SectionProgram testSectionProgram;

    @BeforeEach
    void setUp() {
        testSectionProgram = new SectionProgram();
        testSectionProgram.setId(1L);
        testSectionProgram.setSectionId(1);
        testSectionProgram.setProgramId(2);
    }

    @Test
    void shouldSearchSectionPrograms() throws Exception {
        when(sectionProgramService.searchSectionProgram(any(SectionProgramSearchRequest.class)))
                .thenReturn(List.of(testSectionProgram));

        mockMvc.perform(get("/api/section-program")
                .param("sectionId", "1")
                .param("programId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("SectionProgram results retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].sectionId").value(1))
                .andExpect(jsonPath("$.data[0].programId").value(2));

        verify(sectionProgramService).searchSectionProgram(any(SectionProgramSearchRequest.class));
    }

    @Test
    void shouldCreateSectionProgram() throws Exception {
        when(sectionProgramService.createSectionProgram(anyInt(), anyInt()))
                .thenReturn(testSectionProgram);

        mockMvc.perform(post("/api/section-program")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(testSectionProgram)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("SectionProgram created successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(sectionProgramService).createSectionProgram(
                testSectionProgram.getSectionId(),
                testSectionProgram.getProgramId());
    }

    @Test
    void shouldReturnErrorWhenDuplicateSectionProgramExists() throws Exception {
        when(sectionProgramService.createSectionProgram(anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException(
                        "This section is already assigned to the given program."));

        mockMvc.perform(post("/api/section-program")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(testSectionProgram)))
                .andExpect(status().isBadRequest());

        verify(sectionProgramService).createSectionProgram(anyInt(), anyInt());
    }

    @Test
    void shouldDeleteSectionProgram() throws Exception {
        doNothing().when(sectionProgramService).removeSectionProgram(1L);

        mockMvc.perform(delete("/api/section-program/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("SectionProgram deleted successfully"));

        verify(sectionProgramService).removeSectionProgram(1L);
    }
}