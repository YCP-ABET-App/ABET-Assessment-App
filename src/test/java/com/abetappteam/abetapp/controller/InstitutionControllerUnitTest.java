package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.dto.InstitutionDTO;
import com.abetappteam.abetapp.entity.Institution;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.service.InstitutionService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstitutionController.class)
@Import(TestSecurityConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
public class InstitutionControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InstitutionService institutionService;

    private Institution testInstitution;
    private InstitutionDTO testDTO;

    @BeforeEach
    void setUp() {
        testInstitution = new Institution();
        testInstitution.setId(1L);
        testInstitution.setName("Example University");
        testInstitution.setColor(13158);
        testInstitution.setLogo("ycp");

        testDTO = new InstitutionDTO("New Institution", 13158, "logo");
    }

    @Test
    void shouldGetAllInstitutions() throws Exception {
        Page<Institution> page = new PageImpl<>(List.of(testInstitution), PageRequest.of(0, 20), 1);
        when(institutionService.findAll(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/institution")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(institutionService, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void shouldCreateInstitution() throws Exception {
        when(institutionService.create(any(InstitutionDTO.class))).thenReturn(testInstitution);

        mockMvc.perform(post("/api/institution")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Example University"));

        verify(institutionService, times(1)).create(any(InstitutionDTO.class));
    }

    @Test
    void shouldGetInstitutionById() throws Exception {
        when(institutionService.findById(1L)).thenReturn(testInstitution);

        mockMvc.perform(get("/api/institution/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Example University"));

        verify(institutionService, times(1)).findById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenInstitutionDoesNotExist() throws Exception {
        when(institutionService.findById(999L))
                .thenThrow(new ResourceNotFoundException("Institution not found with id: 999"));

        mockMvc.perform(get("/api/institution/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        verify(institutionService, times(1)).findById(999L);
    }

    @Test
    void shouldReturnBadRequestForInvalidInstitution() throws Exception {
        InstitutionDTO invalidDTO = new InstitutionDTO(null, null, null);

        mockMvc.perform(post("/api/institution")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(institutionService, never()).create(any(InstitutionDTO.class));
    }

    @Test
    void shouldUpdateInstitution() throws Exception {
        when(institutionService.update(eq(1L), any(InstitutionDTO.class))).thenReturn(testInstitution);

        mockMvc.perform(put("/api/institution/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Institution updated successfully"));

        verify(institutionService, times(1)).update(eq(1L), any(InstitutionDTO.class));
    }

    @Test
    void shouldDeleteInstitution() throws Exception {
        mockMvc.perform(delete("/api/institution/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Institution deleted successfully"));

        verify(institutionService, times(1)).delete(1L);
    }
}
