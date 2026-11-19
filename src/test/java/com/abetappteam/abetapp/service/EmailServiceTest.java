package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.BaseServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

class EmailServiceTest extends BaseServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Set the @Value fields that Spring would normally inject
        ReflectionTestUtils.setField(emailService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(emailService, "from", "ABET App <no-reply@ycpabet.email>");
    }

    @Test
    void sendRecoveryCode_callsResend() {
        emailService.sendRecoveryCode("to@example.com", "123456");

        verify(restTemplate).postForEntity(
                eq("https://api.resend.com/emails"),
                any(),
                eq(String.class)
        );
    }
}