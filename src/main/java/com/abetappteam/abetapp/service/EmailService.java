package com.abetappteam.abetapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EmailService {

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from:ABET App <no-reply@ycpabet.email>}")
    private String from;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendRecoveryCode(String to, String code) {
        String url = "https://api.resend.com/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "from", from,
                "to", new String[]{to},
                "subject", "Your ABET Assessment recovery code",
                "html", "<p>Your recovery code is: <strong>" + code + "</strong></p>"
                        + "<p>It expires in 15 minutes.</p>"
        );

        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }
}