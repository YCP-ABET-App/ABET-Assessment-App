package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.dto.ProgramDTO;
import com.abetappteam.abetapp.dto.UsersDTO;
import com.abetappteam.abetapp.entity.Program;
import com.abetappteam.abetapp.entity.Users;
import com.abetappteam.abetapp.repository.UsersRepository;
import com.abetappteam.abetapp.security.JwtUtil;
import com.abetappteam.abetapp.service.ProgramService;
import com.abetappteam.abetapp.service.UsersService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SetupController.class)
@Import(TestSecurityConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
class SetupControllerUnitTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean UsersRepository usersRepository;
    @MockitoBean UsersService usersService;
    @MockitoBean ProgramService programService;
    @MockitoBean JwtUtil jwtUtil;

    @Test
    void status_whenNoUsers_returnsNeedsSetupTrue() throws Exception {
        when(usersRepository.count()).thenReturn(0L);

        mockMvc.perform(get("/api/setup/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needsSetup").value(true));
    }

    @Test
    void status_whenUsersExist_returnsNeedsSetupFalse() throws Exception {
        when(usersRepository.count()).thenReturn(5L);

        mockMvc.perform(get("/api/setup/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needsSetup").value(false));
    }

    @Test
    void setup_whenAlreadyCompleted_returns403() throws Exception {
        when(usersRepository.count()).thenReturn(1L);

        UsersDTO dto = new UsersDTO("admin@example.com", "password123", "Admin", "User", null, true);

        mockMvc.perform(post("/api/setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Setup already completed"));
    }

    @Test
    void setup_whenEmpty_createsUserProgramAndReturnsToken() throws Exception {
        when(usersRepository.count()).thenReturn(0L);

        Users created = new Users();
        created.setId(1L);
        created.setEmail("admin@example.com");
        created.setFirstName("Admin");
        created.setLastName("User");

        Program program = new Program();
        program.setId(10L);

        when(usersService.create(any(UsersDTO.class))).thenReturn(created);
        when(programService.create(any(ProgramDTO.class))).thenReturn(program);
        when(jwtUtil.generateToken(eq(1L), eq("admin@example.com"), eq("ADMIN"), eq(10L)))
                .thenReturn("fake-jwt");

        UsersDTO dto = new UsersDTO("admin@example.com", "password123", "Admin", "User", null, true);

        mockMvc.perform(post("/api/setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authToken").value("fake-jwt"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.email").value("admin@example.com"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andExpect(jsonPath("$.user.currentProgramId").value(10));

        verify(programService).addUserToProgram(1L, 10L, true);
    }
}