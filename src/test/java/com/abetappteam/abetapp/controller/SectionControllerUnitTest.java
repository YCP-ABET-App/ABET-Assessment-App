package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.BaseControllerTest;
import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.dto.SectionDTO;
import com.abetappteam.abetapp.entity.Course;
import com.abetappteam.abetapp.entity.Requests.Course.CourseSearchRequest;
import com.abetappteam.abetapp.entity.Requests.Section.SectionSearchRequest;
import com.abetappteam.abetapp.entity.Requests.Section.SectionSearchResponse;
import com.abetappteam.abetapp.entity.Section;
import com.abetappteam.abetapp.service.CourseService;
import com.abetappteam.abetapp.service.SectionService;
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

@WebMvcTest(SectionController.class)
@Import(TestSecurityConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
class SectionControllerUnitTest extends BaseControllerTest {

    @MockitoBean
    private SectionService sectionService;

    @MockitoBean
    private CourseService courseService;

    private Section testSection;
    private Course testCourse;
    private SectionDTO testDTO;

    @BeforeEach
    void setUp() {
        testSection = new Section();
        testSection.setId(1L);
        testSection.setSectionNumber("001");
        testSection.setCourseId(1);
        testSection.setSemesterId(1);

        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setCourseCode("CS400");
        testCourse.setCourseName("Software Engineering");
        testCourse.setCourseDescription("Test course");
        testCourse.setIsActive(true);

        testDTO = new SectionDTO("001", 1, 1, null);
        testDTO.setSectionNumber("001");
    }

    @Test
    void shouldSearchSections() throws Exception {
        SectionSearchResponse response = new SectionSearchResponse(List.of(testSection), List.of(testCourse));

        when(sectionService.searchSections(any(SectionSearchRequest.class))).thenReturn(List.of(testSection));
        when(courseService.searchCourse(any(CourseSearchRequest.class))).thenReturn(List.of(testCourse));

        mockMvc.perform(get("/api/section")
                .param("courseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sections retrieved successfully for course"))
                .andExpect(jsonPath("$.data.sections[0].id").value(1))
                .andExpect(jsonPath("$.data.courses[0].courseCode").value("CS400"));

        verify(sectionService).searchSections(any(SectionSearchRequest.class));
        verify(courseService).searchCourse(any(CourseSearchRequest.class));
    }

    @Test
    void shouldReturnEmptyResponseWhenNoSectionsFound() throws Exception {
        when(sectionService.searchSections(any(SectionSearchRequest.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/section"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sections").isEmpty())
                .andExpect(jsonPath("$.data.courses").isEmpty());

        verify(sectionService).searchSections(any(SectionSearchRequest.class));
        verify(courseService, never()).searchCourse(any());
    }

    @Test
    void shouldCreateSection() throws Exception {
        when(sectionService.createSection(anyString(), anyInt(), anyInt())).thenReturn(testSection);

        mockMvc.perform(post("/api/section")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(testDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Section created successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(sectionService).createSection(
                testDTO.getSectionNumber(),
                testDTO.getCourseId(),
                testDTO.getSemesterId());
    }

    @Test
    void shouldUpdateSection() throws Exception {
        when(sectionService.updateSection(eq(1L), anyString(), anyInt(), anyInt())).thenReturn(testSection);

        mockMvc.perform(put("/api/section/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(testDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Section updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(sectionService).updateSection(eq(1L), anyString(), anyInt(), anyInt());
    }

    @Test
    void shouldDeleteSection() throws Exception {
        doNothing().when(sectionService).removeSection(1L);

        mockMvc.perform(delete("/api/section/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Section deleted successfully"));

        verify(sectionService).removeSection(1L);
    }
}