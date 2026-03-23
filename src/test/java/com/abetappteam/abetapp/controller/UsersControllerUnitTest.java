package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.config.TestSecurityConfig;
import com.abetappteam.abetapp.dto.UpdateUsersDTO;
import com.abetappteam.abetapp.entity.Program;
import com.abetappteam.abetapp.entity.ProgramUser;
import com.abetappteam.abetapp.entity.Users;
import com.abetappteam.abetapp.dto.UsersDTO;
import com.abetappteam.abetapp.security.JwtUtil;
import com.abetappteam.abetapp.service.CourseService;
import com.abetappteam.abetapp.service.ProgramService;
import com.abetappteam.abetapp.service.UsersService;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsersController.class)
@Import(TestSecurityConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
public class UsersControllerUnitTest {
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private UsersService userService;

        @MockitoBean
        private CourseService courseService;

        @MockitoBean
        private ProgramService programService;

        @MockitoBean
        private JwtUtil jwtUtil;

        @MockitoBean
        private BCryptPasswordEncoder passwordEncoder;

        private Users testUser;
        private UsersDTO testDTO;
        private ProgramUser testProgramUser;
        private Program testProgram;

        @BeforeEach
        void setUp() {
                testUser = new Users();
                testUser.setId(1L);
                testUser.setEmail("rwade4@ycp.edu");
                testUser.setPasswordHash("TEMP");
                testUser.setFirstName("Test");
                testUser.setLastName("User");
                testUser.setTitle("Dr.");
                testUser.setActive(true);

                testDTO = new UsersDTO("NewEmail@gmail.com", "NewPassword", "NewFirstName", "NewLastName", "NewTitle", true);
                testDTO.setEmail("NewEmail@gmail.com");
                testDTO.setPasswordHash("NewPassword");
                testDTO.setFirstName("NewFirstName");
                testDTO.setLastName("NewLastName");
                testDTO.setTitle("NewTitle");
                testDTO.setActive(true);

                testProgramUser = new ProgramUser(true, 1L, 1L);
                testProgramUser.setId(1L);
                testProgramUser.setIsActive(true);

                testProgram = new Program();
                testProgram.setId(1L);
                testProgram.setName("Test Program");
                testProgram.setInstitution("Test University");
                testProgram.setActive(true);
        }

        @Test
        void shouldGetAllUsers() throws Exception {
                List<Users> users = List.of(testUser);
                Page<Users> page = new PageImpl<>(users, PageRequest.of(0, 20), 1);

                when(userService.findAll(any(PageRequest.class))).thenReturn(page);

                mockMvc.perform(get("/api/users")
                                .param("page", "0")
                                .param("size", "20"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content.length()").value(1))
                                .andExpect(jsonPath("$.totalElements").value(1));

                verify(userService, times(1)).findAll(any(PageRequest.class));
        }

        @Test
        void shouldCreateUser() throws Exception {
                when(userService.create(any(UsersDTO.class))).thenReturn(testUser);

                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Resource created successfully"))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.firstName").value("Test"));
                verify(userService, times(1)).create(any(UsersDTO.class));
        }

        @Test
        void shouldGetUserById() throws Exception {
                when(userService.findById(1L)).thenReturn(testUser);

                mockMvc.perform(get("/api/users/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.firstName").value("Test"));

                verify(userService, times(1)).findById(1L);
        }

        @Test
        void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
                // Given
                when(userService.findById(999L))
                                .thenThrow(new ResourceNotFoundException("User not found with id: 999"));

                // When/Then
                mockMvc.perform(get("/api/users/999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error").value("User not found with id: 999"));

                verify(userService, times(1)).findById(999L);
        }

        @Test
        void shouldReturnBadRequestForInvalidUser() throws Exception {
                // Given - DTO with missing required fields and invalid fields
                UsersDTO invalidDTO = new UsersDTO("email", null, null, null, "V;vmiiQ94S$npW0g+6A2UpEB3bW.nvm)F&$#S2dfrK?pC!P]xuU", true);
                invalidDTO.setFirstName(null); // Invalid - first name is required
                invalidDTO.setLastName(null); // Invalid - last name is required
                invalidDTO.setTitle("V;vmiiQ94S$npW0g+6A2UpEB3bW.nvm)F&$#S2dfrK?pC!P]xuU"); // Invalid - title cannot be
                                                                                            // more
                                                                                            // than 50 characters long
                invalidDTO.setEmail("email"); // Invalid - a valid email address is required
                invalidDTO.setPasswordHash(null); // Invalid - password is required
                invalidDTO.setActive(true);

                // When/Then
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDTO)))
                                .andExpect(status().isBadRequest());

                // Service should not be called for invalid input
                verify(userService, never()).create(any(UsersDTO.class));
        }

        @Test
        void shouldUpdateUser() throws Exception {
                // Given
                Long userId = 1L;

                Users existingUser = new Users();
                existingUser.setId(userId);
                existingUser.setEmail("old@example.com");
                existingUser.setFirstName("OldFirst");
                existingUser.setLastName("OldLast");

                Users updatedUser = new Users();
                updatedUser.setId(userId);
                updatedUser.setEmail("NewEmail@gmail.com");
                updatedUser.setFirstName("NewFirstName");
                updatedUser.setLastName("NewLastName");
                updatedUser.setTitle("NewTitle");
                updatedUser.setActive(true);

                // IMPORTANT: Mock the UpdateUsersDTO overload, not the UsersDTO one
                when(userService.update(eq(userId), any(UpdateUsersDTO.class)))
                                .thenReturn(updatedUser);

                // When/Then
                mockMvc.perform(put("/api/users/{id}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"NewEmail@gmail.com\"," +
                                                "\"firstName\":\"NewFirstName\"," +
                                                "\"lastName\":\"NewLastName\"," +
                                                "\"title\":\"NewTitle\"," +
                                                "\"active\":true}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value(userId))
                                .andExpect(jsonPath("$.data.email").value("NewEmail@gmail.com"));
        }

        @Test
        void shouldDeleteUser() throws Exception {
                // Given - no need to mock void method with doNothing, it's the default

                // When/Then
                mockMvc.perform(delete("/api/users/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("User deleted successfully"));

                verify(userService, times(1)).delete(1L);
        }

        @Test
        void shouldLoginSuccessfully() throws Exception {
                // Given
                Map<String, String> credentials = new HashMap<>();
                credentials.put("email", "rwade4@ycp.edu");
                credentials.put("password", "password123");

                when(userService.findByEmail("rwade4@ycp.edu")).thenReturn(testUser);
                when(passwordEncoder.matches("password123", "TEMP")).thenReturn(true);
                when(programService.getActiveProgramsForUser(1L)).thenReturn(List.of(testProgramUser));
                when(programService.getDefaultProgramForUser(1L)).thenReturn(testProgramUser);
                when(courseService.hasActiveCourses(1L)).thenReturn(true);
                when(jwtUtil.generateToken(1L, "rwade4@ycp.edu", "ADMIN", 1L)).thenReturn("test-token");

                // When/Then
                mockMvc.perform(post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(credentials)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.authToken").value("test-token"))
                                .andExpect(jsonPath("$.user.id").value(1))
                                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                                .andExpect(jsonPath("$.hasCourses").value(true));
        }

        @Test
        void shouldFailLoginWithInvalidPassword() throws Exception {
                // Given
                Map<String, String> credentials = new HashMap<>();
                credentials.put("email", "rwade4@ycp.edu");
                credentials.put("password", "wrongpassword");

                when(userService.findByEmail("rwade4@ycp.edu")).thenReturn(testUser);
                when(passwordEncoder.matches("wrongpassword", "TEMP")).thenReturn(false);

                // When/Then
                mockMvc.perform(post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(credentials)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldSignupSuccessfully() throws Exception {
                // Given
                when(passwordEncoder.encode("NewPassword")).thenReturn("hashedPassword");
                when(userService.create(any(UsersDTO.class))).thenReturn(testUser);
                when(programService.getDefaultProgramForUser(1L)).thenReturn(testProgramUser);
                when(courseService.hasActiveCourses(1L)).thenReturn(true);
                when(jwtUtil.generateToken(1L, "rwade4@ycp.edu", "ADMIN", 1L)).thenReturn("test-token");

                // When/Then
                mockMvc.perform(post("/api/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.authToken").value("test-token"))
                                .andExpect(jsonPath("$.user.id").value(1))
                                .andExpect(jsonPath("$.hasCourses").value(true));
        }

        @Test
        void shouldSwitchProgram() throws Exception {
                // Given
                Map<String, Long> request = new HashMap<>();
                request.put("programId", 2L);

                when(jwtUtil.extractUserId("test-token")).thenReturn(1L);
                when(programService.getProgramUser(1L, 2L)).thenReturn(testProgramUser);
                when(courseService.hasActiveCourses(1L)).thenReturn(false);
                when(userService.findById(1L)).thenReturn(testUser);
                when(jwtUtil.generateToken(1L, "rwade4@ycp.edu", "ADMIN", 2L)).thenReturn("new-token");

                // When/Then
                mockMvc.perform(post("/api/users/switch-program")
                                .header("Authorization", "Bearer test-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("new-token"))
                                .andExpect(jsonPath("$.role").value("ADMIN"))
                                .andExpect(jsonPath("$.programId").value(2))
                                .andExpect(jsonPath("$.hasCourses").value(false));
        }

        @Test
        void shouldGetMyPrograms() throws Exception {
                // Given
                when(jwtUtil.extractUserId("test-token")).thenReturn(1L);
                when(programService.getActiveProgramsForUser(1L)).thenReturn(List.of(testProgramUser));
                when(programService.findById(1L)).thenReturn(testProgram);

                // When/Then
                mockMvc.perform(get("/api/users/my-programs")
                                .header("Authorization", "Bearer test-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].programId").value(1))
                                .andExpect(jsonPath("$.data[0].programName").value("Test Program"))
                                .andExpect(jsonPath("$.data[0].isAdmin").value(true));
        }
}