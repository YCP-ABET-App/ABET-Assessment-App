package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.BaseControllerTest;
import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.entity.Requests.SectionUser.SectionUserSearchRequest;
import com.abetappteam.abetapp.entity.SectionUser;
import com.abetappteam.abetapp.service.SectionUserService;
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

@WebMvcTest(SectionUserController.class)
@Import(TestSecurityConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
class SectionUserControllerUnitTest extends BaseControllerTest {

    @MockitoBean
    private SectionUserService sectionUserService;

    private SectionUser testSectionUser;

    @BeforeEach
    void setUp() {
        testSectionUser = new SectionUser();
        testSectionUser.setId(1L);
        testSectionUser.setSectionId(1);
        testSectionUser.setUserId(2);
    }

    @Test
    void shouldSearchSectionUsers() throws Exception {
        when(sectionUserService.searchSectionUser(any(SectionUserSearchRequest.class)))
                .thenReturn(List.of(testSectionUser));

        mockMvc.perform(get("/api/section-user")
                .param("sectionId", "1")
                .param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("SectionUser results retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].sectionId").value(1))
                .andExpect(jsonPath("$.data[0].userId").value(2));

        verify(sectionUserService).searchSectionUser(any(SectionUserSearchRequest.class));
    }

    @Test
    void shouldCreateSectionUser() throws Exception {
        when(sectionUserService.createSectionUser(anyInt(), anyInt()))
                .thenReturn(testSectionUser);

        mockMvc.perform(post("/api/section-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(testSectionUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("SectionUser created successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(sectionUserService).createSectionUser(
                testSectionUser.getSectionId(),
                testSectionUser.getUserId());
    }

    @Test
    void shouldReturnErrorWhenDuplicateSectionUserExists() throws Exception {
        when(sectionUserService.createSectionUser(anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException(
                        "This user is already assigned to the given section."));

        mockMvc.perform(post("/api/section-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(testSectionUser)))
                .andExpect(status().isBadRequest());

        verify(sectionUserService).createSectionUser(anyInt(), anyInt());
    }

    @Test
    void shouldDeleteSectionUser() throws Exception {
        doNothing().when(sectionUserService).removeSectionUser(1L);

        mockMvc.perform(delete("/api/section-user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("SectionUser deleted successfully"));

        verify(sectionUserService).removeSectionUser(1L);
    }
}