package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.BaseServiceTest;
import com.abetappteam.abetapp.entity.Users;
import com.abetappteam.abetapp.exception.BadRequestException;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RecoveryServiceTest extends BaseServiceTest {

    @Mock UsersService usersService;
    @Mock EmailService emailService;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks RecoveryService recoveryService;

    @Test
    void sendRecoveryCode_userExists_sendsEmailAndStoresCode() {
        Users user = new Users();
        user.setEmail("user@example.com");
        when(usersService.findByEmail("user@example.com")).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-code");

        recoveryService.sendRecoveryCode("user@example.com");

        verify(emailService).sendRecoveryCode(eq("user@example.com"), anyString());
        verify(passwordEncoder).encode(anyString());
    }

    @Test
    void sendRecoveryCode_userNotFound_throwsBadRequest() {
        when(usersService.findByEmail(anyString()))
                .thenThrow(new ResourceNotFoundException("not found"));

        assertThatThrownBy(() -> recoveryService.sendRecoveryCode("missing@example.com"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void resetPassword_invalidCode_throws() {
        // no code stored
        assertThatThrownBy(() ->
                recoveryService.resetPassword("user@example.com", "000000", "newpass"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid or expired");
    }

    @Test
    void verifyAndConsumeCode_oneTimeUse() {
        Users user = new Users();
        user.setEmail("user@example.com");
        when(usersService.findByEmail("user@example.com")).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(passwordEncoder.matches(anyString(), eq("hashed"))).thenReturn(true);

        recoveryService.sendRecoveryCode("user@example.com");

        // first use succeeds
        assertThat(recoveryService.verifyAndConsumeCode("user@example.com", "any")).isTrue();
        // second use fails (consumed)
        assertThat(recoveryService.verifyAndConsumeCode("user@example.com", "any")).isFalse();
    }
}