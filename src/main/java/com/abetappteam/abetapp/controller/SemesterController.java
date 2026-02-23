package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.dto.PagedResponse;
import com.abetappteam.abetapp.dto.SemesterDTO;
import com.abetappteam.abetapp.entity.Requests.Semester.SemesterSearchRequest;
import com.abetappteam.abetapp.entity.Semester;
import com.abetappteam.abetapp.entity.Semester.SemesterStatus;
import com.abetappteam.abetapp.entity.Semester.SemesterType;
import com.abetappteam.abetapp.service.SemesterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Semester entity operations
 * Manages semesters, academic periods, and semester status
 */
@RestController
@RequestMapping("/api/semesters")
public class SemesterController extends BaseController {

    @Autowired
    private SemesterService semesterService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Semester>>> getAllSemesters(@RequestBody SemesterSearchRequest request) {
        logger.info("Fetching all semesters");
        List<Semester> semesters = semesterService.getAllSemesters(request);
        return success(semesters, "Semesters retrieved successfully");
    }

    /**
     * Create a new semester
     */
    @PostMapping
    public ResponseEntity<?> createSemester(
            @Valid @RequestBody SemesterDTO dto,
            BindingResult result) {

        if (result.hasErrors()) {
            return validationError(result);
        }

        logger.info("Creating new semester: {} ({})", dto.getName(), dto.getCode());
        Semester semester = semesterService.createSemester(dto);
        return created(semester);
    }

    /**
     * Update an existing semester
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Semester>> updateSemester(
            @PathVariable Long id,
            @Valid @RequestBody SemesterDTO dto) {

        logger.info("Updating semester with ID: {}", id);
        validateId(id);
        Semester updated = semesterService.updateSemester(id, dto);
        return success(updated, "Semester updated successfully");
    }

    /**
     * Remove/delete a semester
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeSemester(@PathVariable Long id) {
        logger.info("Removing semester with ID: {}", id);
        validateId(id);
        semesterService.removeSemester(id);
        return success(null, "Semester removed successfully");
    }



    /**
     * Set a semester as current for a program
     */
    @PostMapping("/{id}/set-current")
    public ResponseEntity<ApiResponse<Semester>> setAsCurrentSemester(@PathVariable Long id) {
        logger.info("Setting semester {} as current", id);
        validateId(id);
        Semester updated = semesterService.setAsCurrentSemester(id);
        return success(updated, "Semester set as current successfully");
    }

    /**
     * Update semester status
     */
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<ApiResponse<Semester>> updateSemesterStatus(
            @PathVariable Long id,
            @PathVariable String status) {

        logger.info("Updating semester {} status to: {}", id, status);
        validateId(id);
        SemesterStatus newStatus = SemesterStatus.valueOf(status.toUpperCase());
        Semester updated = semesterService.updateSemesterStatus(id, newStatus);
        return success(updated, "Semester status updated successfully");
    }

    /**
     * Update all semester statuses based on current date
     */
    @PostMapping("/update-statuses")
    public ResponseEntity<ApiResponse<Void>> updateAllSemesterStatuses() {
        logger.info("Updating all semester statuses based on current date");
        semesterService.updateAllSemesterStatuses();
        return success(null, "All semester statuses updated successfully");
    }

    /**
     * Clear current semester flag for a program
     */
    @DeleteMapping("/program/{programId}/clear-current")
    public ResponseEntity<ApiResponse<Void>> clearCurrentSemesterFlag(@PathVariable Long programId) {
        logger.info("Clearing current semester flag for program ID: {}", programId);
        validateId(programId);
        semesterService.clearCurrentSemesterFlag(programId);
        return success(null, "Current semester flag cleared successfully");
    }
}