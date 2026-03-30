package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.BaseControllerTest;
import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.dto.CourseDTO;
import com.abetappteam.abetapp.entity.Course;
import com.abetappteam.abetapp.entity.Requests.Course.CourseSearchRequest;
import com.abetappteam.abetapp.service.CourseService;
import com.abetappteam.abetapp.util.TestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for CourseController - No database involved
 * Uses MockMvc to test the controller layer with mocked service
 */
@WebMvcTest(CourseController.class)
@Import(TestSecurityConfig.class)
class CourseControllerUnitTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourseService courseService;

    private Course testCourse;
    private CourseDTO testCourseDTO;

    @BeforeEach
    void setUp() {
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setCourseCode("CS401");
        testCourse.setCourseName("Software Engineering");
        testCourse.setCourseDescription("An introduction to software engineering principles");
        testCourse.setStudentCount(28);
        testCourse.setIsActive(true);

        testCourseDTO = new CourseDTO();
        testCourseDTO.setCourseCode("CS401");
        testCourseDTO.setCourseName("Software Engineering");
        testCourseDTO.setCourseDescription("An introduction to software engineering principles");
        testCourseDTO.setStudentCount(28);
    }

    // @Test
    // void shouldGetCourseById() throws Exception {
    // // Use the search endpoint (CourseSearchRequest) to retrieve the course by id
    // List<Course> list = List.of(testCourse);
    // when(courseService.searchCourse(any())).thenReturn(list);
    //
    // Map<String, Object> body = new HashMap<>();
    // body.put("id", 1);
    // body.put("courseCode", null);
    // body.put("courseName", null);
    // body.put("courseDescription", null);
    // body.put("student_count", null);
    // body.put("mirrorId", null);
    // body.put("isActive", null);
    //
    // mockMvc.perform(get("/api/courses/searchCourse")
    // .contentType(MediaType.APPLICATION_JSON)
    // .content(objectMapper.writeValueAsString(body)))
    // .andExpect(status().isOk())
    // .andExpect(jsonPath("$.success").value(true))
    // .andExpect(jsonPath("$.data[0].id").value(1))
    // .andExpect(jsonPath("$.data[0].courseName").value("Software Engineering"))
    // .andExpect(jsonPath("$.data[0].courseCode").value("CS401"));
    //
    // verify(courseService, times(1)).searchCourse(any());
    // }

    @Test
    void shouldCreateCourse() throws Exception {
        // Given
        when(courseService.createCourse(any(CourseDTO.class))).thenReturn(testCourse);

        // When/Then
        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCourseDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resource created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.courseName").value("Software Engineering"))
                .andExpect(jsonPath("$.data.courseCode").value("CS401"));

        verify(courseService, times(1)).createCourse(any(CourseDTO.class));
    }

    @Test
    void shouldReturnBadRequestForInvalidCourse() throws Exception {
        // Given - DTO with missing required fields
        CourseDTO invalidDTO = new CourseDTO();
        invalidDTO.setCourseName(null); // Invalid - courseName is required
        invalidDTO.setCourseCode(null); // Invalid - courseCode is required

        // When/Then
        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(courseService, never()).createCourse(any(CourseDTO.class));
    }

    @Test
    void shouldUpdateCourse() throws Exception {
        // Given
        when(courseService.updateCourse(eq(1L), any(CourseDTO.class))).thenReturn(testCourse);

        // When/Then
        mockMvc.perform(put("/api/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCourseDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Course updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(courseService, times(1)).updateCourse(eq(1L), any(CourseDTO.class));
    }

    @Test
    void shouldUpdateStudentCount() throws Exception {
        // Given
        testCourse.setStudentCount(32);
        when(courseService.updateStudentCount(eq(1L), eq(32))).thenReturn(testCourse);

        // When/Then
        mockMvc.perform(put("/api/courses/1/student-count")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentCount\": 32}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentCount").value(32));

        verify(courseService, times(1)).updateStudentCount(1L, 32);
    }

    @Test
    void shouldRemoveCourse() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Course removed successfully"));

        verify(courseService, times(1)).removeCourse(1L);
    }

    @Test
    void shouldDeactivateCourse() throws Exception {
        // Given
        when(courseService.findById(1L)).thenReturn(testCourse);

        // When/Then
        mockMvc.perform(put("/api/courses/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Course deactivated successfully"));

        verify(courseService, times(1)).deactivateCourse(1L);
        verify(courseService, times(1)).findById(1L);
    }

    @Test
    void shouldActivateCourse() throws Exception {
        // Given
        when(courseService.findById(1L)).thenReturn(testCourse);

        // When/Then
        mockMvc.perform(put("/api/courses/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Course activated successfully"));

        verify(courseService, times(1)).activateCourse(1L);
        verify(courseService, times(1)).findById(1L);
    }

    @Test
    void shouldAssignInstructor() throws Exception {
        mockMvc.perform(post("/api/courses/1/instructors/2"))
                .andExpect(status().isOk());

        verify(courseService).assignInstructor(1L, 2L);
    }

    @Test
    void shouldRemoveInstructor() throws Exception {
        mockMvc.perform(delete("/api/courses/1/instructors/2"))
                .andExpect(status().isOk());

        verify(courseService).removeInstructor(1L, 2L);
    }

    @Test
    void shouldAssignIndicator() throws Exception {
        mockMvc.perform(post("/api/courses/1/indicators/10"))
                .andExpect(status().isOk());

        verify(courseService, times(1)).assignIndicator(1L, 10L);
    }

    @Test
    void shouldRemoveIndicator() throws Exception {
        mockMvc.perform(delete("/api/courses/1/indicators/10"))
                .andExpect(status().isOk());

        verify(courseService, times(1)).removeIndicator(1L, 10L);
    }

    @Test
    void shouldSearchCourseWithFilters() throws Exception {
        // Given
        List<Course> list = List.of(testCourse);
        when(courseService.searchCourse(any(CourseSearchRequest.class))).thenReturn(list);

        mockMvc.perform(get("/api/courses/searchCourse")
                .param("courseCode", "CS401")
                .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].courseCode").value("CS401"));

        verify(courseService).searchCourse(any(CourseSearchRequest.class));
    }

    @Test
    void shouldReturnErrorForInvalidIdFormat() throws Exception {

        mockMvc.perform(put("/api/courses/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCourseDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSearchCoursesWithFilters() throws Exception {
        List<Course> list = List.of(testCourse);
        when(courseService.searchCourse(any(CourseSearchRequest.class))).thenReturn(list);

        mockMvc.perform(get("/api/courses/searchCourse")
                .param("courseCode", "CS401")
                .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].courseCode").value("CS401"));

        verify(courseService).searchCourse(any(CourseSearchRequest.class));
    }

    @Test
    void shouldAssignIndicatorToCourse() throws Exception {
        mockMvc.perform(post("/api/courses/1/indicators/10"))
                .andExpect(status().isOk());

        verify(courseService).assignIndicator(1L, 10L);
    }

    @Test
    void shouldRemoveIndicatorFromCourse() throws Exception {
        mockMvc.perform(delete("/api/courses/1/indicators/10"))
                .andExpect(status().isOk());

        verify(courseService).removeIndicator(1L, 10L);
    }

    @Test
    void shouldAssignInstructorToCourse() throws Exception {
        mockMvc.perform(post("/api/courses/1/instructors/5"))
                .andExpect(status().isOk());

        verify(courseService).assignInstructor(1L, 5L);
    }

    @Test
    void shouldVersionCourse() throws Exception {
        // Given
        Course newVersion = TestDataBuilder.createCourseWithId(2L, "CS401-V2", "Software Engineering v2",
                "Updated description", 1L);
        when(courseService.versionCourse(eq(1L), any(CourseDTO.class))).thenReturn(newVersion);

        // When/Then
        mockMvc.perform(post("/api/courses/1/version")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCourseDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.courseCode").value("CS401-V2"));

        verify(courseService).versionCourse(eq(1L), any(CourseDTO.class));
    }
}
