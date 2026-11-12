package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.service.RecoveryService;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthRecoveryController.class)
@Import(TestSecurityConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
class AuthRecoveryControllerUnitTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean RecoveryService recoveryService;

    @Test
    void send_missingEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/recovery/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is required"));

        verifyNoInteractions(recoveryService);
    }

    @Test
    void send_validEmail_returnsGenericSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/recovery/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "user@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("If an account exists, a recovery code has been sent."));

        verify(recoveryService).sendRecoveryCode("user@example.com");
    }

    @Test
    void reset_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/recovery/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "user@example.com"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reset_valid_returnsSuccess() throws Exception {
        Map<String, String> body = Map.of(
                "email", "user@example.com",
                "code", "123456",
                "newPassword", "newPass123"
        );

        mockMvc.perform(post("/api/auth/recovery/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully."));

        verify(recoveryService).resetPassword("user@example.com", "123456", "newPass123");
    }
}   