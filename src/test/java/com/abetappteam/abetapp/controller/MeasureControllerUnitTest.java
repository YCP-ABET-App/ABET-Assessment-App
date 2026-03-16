package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.entity.Course;
import com.abetappteam.abetapp.entity.CourseIndicator;
import com.abetappteam.abetapp.entity.Measure;
import com.abetappteam.abetapp.dto.MeasureDTO;
import com.abetappteam.abetapp.service.MeasureService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeasureController.class)
@Import(TestSecurityConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
public class MeasureControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MeasureService service;

    private Measure testMeasure;
    private MeasureDTO testDTO;
    private CourseIndicator testIndicator;
    private Course testCourse;

    @BeforeEach
    void setUp(){
        testMeasure = new Measure();
        testMeasure.setId(1l);
        testMeasure.setDescription("Test Description");
        testMeasure.setRecommendedAction("Test Action");
        testMeasure.setFcar("Test Fcar");
        testMeasure.setCourseIndicatorId(1l);
        testMeasure.setStatus("InProgress");
        testMeasure.setActive(true);

        testDTO = new MeasureDTO(1L, "New Description", null, "InReview", true);

        testIndicator = new CourseIndicator();
        testIndicator.setId(1l);
        testIndicator.setCourseId(1l);
        testIndicator.setIndicatorId(1l);
        testIndicator.setIsActive(true);

        testCourse = new Course();
        testCourse.setId(1l);
        testCourse.setCourseCode("CS400");
        testCourse.setCourseDescription("Test for Measures");
        testCourse.setIsActive(true);
    }
// TODO: Refactor these tests with updated search code
//    @Test
//    void shouldGetAllMeasures() throws Exception {
//        //Given
//        List<Measure> measures = List.of(testMeasure);
//
//        // The controller expects a MeasureSearchRequest in the request body. Send a body with null filters.
//        Map<String, Object> body = new HashMap<>();
//        body.put("id", null);
//        body.put("courseIndicatorId", null);
//        body.put("active", null);
//
//        when(service.searchMeasures(any())).thenReturn(measures);
//
//        //When/Then
//        mockMvc.perform(get("/api/measure")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(body)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data[0].id").value(1))
//                .andExpect(jsonPath("$.data[0].description").value("Test Description"));
//
//        verify(service, times(1)).searchMeasures(any());
//    }

    @Test
    void shouldCreateMeasure() throws Exception {
        //Given
        when(service.create(any(MeasureDTO.class))).thenReturn(testMeasure);

        //When/Then
        mockMvc.perform(post("/api/measure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Resource created successfully"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.description").value("Test Description"));
        verify(service, times(1)).create(any(MeasureDTO.class));
    }

//    @Test
//    void shouldGetMeasurebyId() throws Exception {
//        //Given
//        // The controller's GET /api/measure endpoint accepts a MeasureSearchRequest in the request body.
//        // Create a JSON body with only id = 1 and null for the other fields.
//        Map<String, Object> body = new HashMap<>();
//        body.put("id", 1);
//        body.put("courseIndicatorId", null);
//        body.put("active", null);
//
//        when(service.searchMeasures(any())).thenReturn(List.of(testMeasure));
//
//        //When/Then
//        mockMvc.perform(get("/api/measure")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(body)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                // controller returns a list of measures in the "data" field
//                .andExpect(jsonPath("$.data[0].id").value(1))
//                .andExpect(jsonPath("$.data[0].description").value("Test Description"));
//
//        verify(service, times(1)).searchMeasures(any());
//    }
//
//    @Test
//    void shouldReturnNotFoundWhenMeasureDoesNotExist() throws Exception {
//        // Given: searching for id=999 returns no results
//        Map<String, Object> body = new HashMap<>();
//        body.put("id", 999);
//        body.put("courseIndicatorId", null);
//        body.put("active", null);
//
//        when(service.searchMeasures(any())).thenReturn(List.of());
//
//        // When/Then - controller returns success with empty data for no matches
//        mockMvc.perform(get("/api/measure")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(body)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data").isArray())
//                .andExpect(jsonPath("$.data.length()").value(0));
//
//        verify(service, times(1)).searchMeasures(any());
//    }

    @Test
    void shouldReturnBadRequestWhenDescriptionIsMissing() throws Exception {
        testDTO.setDescription(null); // Force a validation error

        mockMvc.perform(post("/api/measure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDTO)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    void shouldUpdateMeasure() throws Exception {
        // Given
        when(service.update(eq(1L), any(MeasureDTO.class))).thenReturn(testMeasure);

        // When/Then
        mockMvc.perform(put("/api/measure/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Measure updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(service, times(1)).update(eq(1L), any(MeasureDTO.class));
    }

    @Test
    void shouldDeleteMeasure() throws Exception {
        // Given - no need to mock void method with doNothing, it's the default

        // When/Then
        mockMvc.perform(delete("/api/measure/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Measure deleted successfully"));

        verify(service, times(1)).delete(1L);
    }
// TODO: Come through and refactor these tests with updated search code
//    @Test
//    void shouldReturnAllActiveMeasuresByCourseId() throws Exception {
//        //Given
//        List<Measure> measures = List.of(testMeasure);
//        when(service.findAllActiveMeasuresByCourse(eq(1l))).thenReturn(measures);
//
//        //When
//        mockMvc.perform(get("/api/measure/byCourse/1"))
//                .andExpect(status().isOk())
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("Measures found"))
//                .andExpect(jsonPath("$.data.[0].id").value(1));
//
//        verify(service, times(1)).findAllActiveMeasuresByCourse(1l);
//    }
//
//    @Test
//    void shouldReturnAllActiveMeasuresByIndicatorId() throws Exception {
//        //Given
//        List<Measure> measures = List.of(testMeasure);
//        when(service.findAllActiveMeasuresByIndicator(eq(1l))).thenReturn(measures);
//
//        //When
//        mockMvc.perform(get("/api/measure/byIndicator/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("Measures found"))
//                .andExpect(jsonPath("$.data.[0].id").value(1));
//
//        verify(service, times(1)).findAllActiveMeasuresByIndicator(1l);
//    }




}
