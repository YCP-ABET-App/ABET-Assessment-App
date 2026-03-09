package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.entity.CourseIndicator;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.service.CourseIndicatorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseIndicatorController.class)
@Import(TestSecurityConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
public class CourseIndicatorControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseIndicatorService courseIndicatorService;

    private CourseIndicator testCourseIndicator;

    @BeforeEach
    void setUp() {
        testCourseIndicator = new CourseIndicator();
        testCourseIndicator.setId(1L);
        testCourseIndicator.setCourseId(1L);
        testCourseIndicator.setIndicatorId(1L);
        testCourseIndicator.setIsActive(true);
    }

    @Test
    void shouldSearchCourseIndicatorsWithFilters() throws Exception {
        // Given
        when(courseIndicatorService.searchCourseIndicators(any())).thenReturn(List.of(testCourseIndicator));

        // When/Then
        mockMvc.perform(get("/api/course-indicators")
                .param("courseId", "1")
                .param("indicatorId", "1")
                .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].courseId").value(1))
                .andExpect(jsonPath("$.data[0].isActive").value(true));

        verify(courseIndicatorService).searchCourseIndicators(any());
    }

    @Test
    void shouldGetCourseIndicatorById() throws Exception {
        // Given
        when(courseIndicatorService.findById(1L)).thenReturn(testCourseIndicator);

        // When/Then
        mockMvc.perform(get("/api/course-indicators/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Course indicator retrieved successfully"))
                .andExpect(jsonPath("$.data.courseId").value(1))
                .andExpect(jsonPath("$.data.indicatorId").value(1));

        verify(courseIndicatorService).findById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenCourseIndicatorDoesNotExist() throws Exception {
        // Given
        when(courseIndicatorService.findById(999L))
                .thenThrow(new ResourceNotFoundException("CourseIndicator not found with id: 999"));

        // When/Then
        mockMvc.perform(get("/api/course-indicators/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("CourseIndicator not found with id: 999"));

        verify(courseIndicatorService).findById(999L);
    }

    @Test
    void shouldGetActiveIndicatorsByCourse() throws Exception {
        // Given
        when(courseIndicatorService.findActiveIndicatorsByCourseId(1L)).thenReturn(List.of(testCourseIndicator));

        // When/Then
        mockMvc.perform(get("/api/course-indicators/by-course/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Active indicators retrieved successfully"))
                .andExpect(jsonPath("$.data[0].courseId").value(1));

        verify(courseIndicatorService).findActiveIndicatorsByCourseId(1L);
    }

    @Test
    void shouldGetActiveCoursesByIndicator() throws Exception {
        // Given
        when(courseIndicatorService.findActiveCoursesByIndicatorId(1L)).thenReturn(List.of(testCourseIndicator));

        // When/Then
        mockMvc.perform(get("/api/course-indicators/by-indicator/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Active courses retrieved successfully"))
                .andExpect(jsonPath("$.data[0].indicatorId").value(1));

        verify(courseIndicatorService).findActiveCoursesByIndicatorId(1L);
    }

    @Test
    void shouldActivateCourseIndicator() throws Exception {
        // Given
        when(courseIndicatorService.activate(1L)).thenReturn(testCourseIndicator);

        // When/Then
        mockMvc.perform(put("/api/course-indicators/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Course indicator activated successfully"))
                .andExpect(jsonPath("$.data.isActive").value(true));

        verify(courseIndicatorService).activate(1L);
    }

    @Test
    void shouldDeactivateCourseIndicator() throws Exception {
        // Given
        testCourseIndicator.setIsActive(false);
        when(courseIndicatorService.deactivate(1L)).thenReturn(testCourseIndicator);

        // When/Then
        mockMvc.perform(put("/api/course-indicators/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Course indicator deactivated successfully"))
                .andExpect(jsonPath("$.data.isActive").value(false));

        verify(courseIndicatorService).deactivate(1L);
    }

    @Test
    void shouldCheckHasActiveIndicators() throws Exception {
        // Given
        when(courseIndicatorService.hasActiveIndicators(1L)).thenReturn(true);

        // When/Then
        mockMvc.perform(get("/api/course-indicators/by-course/1/has-active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Active indicator check completed"))
                .andExpect(jsonPath("$.data").value(true));

        verify(courseIndicatorService).hasActiveIndicators(1L);
    }

    @Test
    void shouldCountActiveIndicators() throws Exception {
        // Given
        when(courseIndicatorService.countActiveIndicatorsByCourse(1L)).thenReturn(3L);

        // When/Then
        mockMvc.perform(get("/api/course-indicators/by-course/1/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Active indicator count retrieved successfully"))
                .andExpect(jsonPath("$.data").value(3));

        verify(courseIndicatorService).countActiveIndicatorsByCourse(1L);
    }
}