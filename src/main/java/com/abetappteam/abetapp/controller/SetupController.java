package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ProgramDTO;
import com.abetappteam.abetapp.dto.UsersDTO;
import com.abetappteam.abetapp.entity.Program;
import com.abetappteam.abetapp.entity.Users;
import com.abetappteam.abetapp.repository.UsersRepository;
import com.abetappteam.abetapp.security.JwtUtil;
import com.abetappteam.abetapp.service.ProgramService;
import com.abetappteam.abetapp.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/setup")
public class SetupController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UsersService usersService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/status")
    public Map<String, Boolean> status() {
        boolean needsSetup = usersRepository.count() == 0;
        return Map.of("needsSetup", needsSetup);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> setup(@RequestBody UsersDTO dto) {
        if (usersRepository.count() > 0) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Setup already completed"));
        }

        // UsersService.create already hashes the password
        Users user = usersService.create(dto);

        // Create a default program and make this user an admin
        ProgramDTO programDto = new ProgramDTO("Default Program", "Default Institution", true);
        Program program = programService.create(programDto);
        programService.addUserToProgram(user.getId(), program.getId(), true);

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                "ADMIN",
                program.getId()
        );

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("role", "ADMIN");
        userMap.put("currentProgramId", program.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("authToken", token);
        response.put("user", userMap);

        return ResponseEntity.ok(response);
    }
}