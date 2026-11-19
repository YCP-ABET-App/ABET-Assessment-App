package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/institution")
public class InstitutionLoginController {

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Boolean>> institutionLogin(@RequestParam String institutionId) {
        boolean successfulLogin = Objects.equals(institutionId, "ycp123");
        return ResponseEntity.ok(ApiResponse.success(
                successfulLogin,
                "Institution login " + (successfulLogin ? "successful" : "failed")));
    }
}
