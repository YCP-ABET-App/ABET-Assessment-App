package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/institution")
public class InstitutionLoginController extends BaseController {

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Boolean>> institutionLogin(@RequestParam String institutionId) {
        // Mockup: validate against hardcoded institution code
        logger.info("Institution login attempt with code: {}", institutionId);

        boolean successfulLogin = Objects.equals(institutionId, "ycpAbetCapstoneSpring2026!");

        return success(successfulLogin, "Institution login " + (successfulLogin ? "successful" : "failed"));
    }
}
