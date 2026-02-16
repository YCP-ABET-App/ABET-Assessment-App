package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.dto.PagedResponse;
import com.abetappteam.abetapp.dto.SectionDTO;
import com.abetappteam.abetapp.entity.Section;
import com.abetappteam.abetapp.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controller for Section entity operations
 * Manages course sections within semesters
 */
@RestController
@RequestMapping("/api/sections")
public class SectionController extends BaseController {

    @Autowired
    private SectionService sectionService;

    /**
     * Get sections by semester
     */
    @GetMapping
    public ResponseEntity<PagedResponse<Section>> getSectionsBySemester(
            @RequestParam Long semesterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        logger.info("Fetching sections for semester ID: {}", semesterId);
        validateId(semesterId);
        Pageable pageable = createPageable(page, size, sort, direction);
        Page<Section> sections = sectionService.getSectionsBySemester(semesterId, pageable);
        return pagedSuccess(sections);
    }

    /**
     * Get section by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Section>> getSection(@PathVariable Long id) {
        logger.info("Fetching section with ID: {}", id);
        validateId(id);
        Section section = sectionService.findById(id);
        return success(section, "Section retrieved successfully");
    }

    /**
     * Create a new section
     */
    @PostMapping
    public ResponseEntity<?> createSection(
            @Valid @RequestBody SectionDTO dto,
            BindingResult result) {

        if (result.hasErrors()) {
            return validationError(result);
        }

        logger.info("Creating new section: {}", dto.getName());
        Section section = sectionService.createSection(dto);
        return created(section);
    }

    /**
     * Update an existing section
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Section>> updateSection(
            @PathVariable Long id,
            @Valid @RequestBody SectionDTO dto) {

        logger.info("Updating section with ID: {}", id);
        validateId(id);
        Section updated = sectionService.updateSection(id, dto);
        return success(updated, "Section updated successfully");
    }

    /**
     * Delete a section
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeSection(@PathVariable Long id) {
        logger.info("Removing section with ID: {}", id);
        validateId(id);
        sectionService.removeSection(id);
        return success(null, "Section removed successfully");
    }

    /**
     * Get sections by course
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<Section>>> getSectionsByCourse(
            @PathVariable Long courseId) {

        logger.info("Fetching sections for course ID: {}", courseId);
        validateId(courseId);
        List<Section> sections = sectionService.getSectionsByCourse(courseId);
        return success(sections, "Sections retrieved successfully");
    }

    /**
     * Get sections by instructor
     */
    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<ApiResponse<List<Section>>> getSectionsByInstructor(
            @PathVariable Long instructorId) {

        logger.info("Fetching sections for instructor ID: {}", instructorId);
        validateId(instructorId);
        List<Section> sections = sectionService.getSectionsByInstructor(instructorId);
        return success(sections, "Sections retrieved successfully");
    }

    /**
     * Check if section exists (duplicate validation)
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> existsSection(
            @RequestParam String name,
            @RequestParam Long courseId,
            @RequestParam Long semesterId) {

        logger.info("Checking if section exists: {}", name);
        boolean exists = sectionService.existsByNameAndCourseAndSemester(
                name, courseId, semesterId);
        return success(exists, "Section existence checked successfully");
    }

}

