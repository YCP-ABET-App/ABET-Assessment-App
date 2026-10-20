package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.service.RecoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/recovery")
public class AuthRecoveryController extends BaseController {

    @Autowired
    private RecoveryService recoveryService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> send(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email is required"));
        }

        recoveryService.sendRecoveryCode(email.trim());

        // Always return the same message (don't reveal if the email exists)
        return ResponseEntity.ok(
                Map.of("message", "If an account exists, a recovery code has been sent.")
        );
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> reset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");
        String newPassword = body.get("newPassword");

        if (email == null || email.isBlank() ||
            code == null || code.isBlank() ||
            newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email, code, and new password are required."));
        }

        recoveryService.resetPassword(email.trim(), code.trim(), newPassword);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
        
}





}