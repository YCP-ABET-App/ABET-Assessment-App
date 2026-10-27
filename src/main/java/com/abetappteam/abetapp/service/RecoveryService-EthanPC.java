package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.entity.Users;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.abetappteam.abetapp.exception.BadRequestException;

@Service
public class RecoveryService {

    private static final int CODE_LENGTH = 6;
    private static final long EXPIRY_MINUTES = 15;

    private final Map<String, RecoveryCode> codes = new ConcurrentHashMap<>();

    @Autowired
    private UsersService usersService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void resetPassword(String email, String code, String newPassword) {
        if (!verifyAndConsumeCode(email, code)) {
            throw new BadRequestException("Invalid or expired recovery code.");
        }

        Users user = usersService.findByEmail(email);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        usersService.save(user);
    }

    public void sendRecoveryCode(String email) {
        Users user;
        try {
            user = usersService.findByEmail(email);
        } catch (ResourceNotFoundException e) {
            throw new BadRequestException("Failed to send recovery code. Please try again.");
        }

        String code = generateCode();
        String hash = passwordEncoder.encode(code);
        Instant expiresAt = Instant.now().plusSeconds(EXPIRY_MINUTES * 60);

        codes.put(user.getEmail().toLowerCase(), new RecoveryCode(hash, expiresAt));

        emailService.sendRecoveryCode(user.getEmail(), code);
    }

    public boolean verifyAndConsumeCode(String email, String code) {
        String key = email.toLowerCase();
        RecoveryCode stored = codes.get(key);

        if (stored == null || Instant.now().isAfter(stored.expiresAt)) {
            codes.remove(key);
            return false;
        }

        boolean matches = passwordEncoder.matches(code, stored.hash);
        if (matches) {
            codes.remove(key); // one-time use
        }
        return matches;
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, CODE_LENGTH);
        int num = new SecureRandom().nextInt(bound);
        return String.format("%0" + CODE_LENGTH + "d", num);
    }

    private static class RecoveryCode {
        final String hash;
        final Instant expiresAt;

        RecoveryCode(String hash, Instant expiresAt) {
            this.hash = hash;
            this.expiresAt = expiresAt;
        }
    }
}