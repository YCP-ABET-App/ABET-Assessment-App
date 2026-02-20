package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.dto.SectionDTO;
import com.abetappteam.abetapp.entity.Section;
import com.abetappteam.abetapp.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for section entity operations
 * Manages section
 */
@RestController
@RequestMapping("/api/section")
public class SectionController extends BaseController {

    @Autowired
    private SectionService sectionService;

    @GetMapping("/semester")
    public ResponseEntity<ApiResponse<List<Section>>> getSectionBySemester(
            @RequestParam int semesterId ) {
        logger.info("Fetching sections for semester id: {}", semesterId);
        long id = semesterId;
        validateId(id);
        List<Section> sections = sectionService.getSectionsBySemesterId(semesterId);
        return success(sections, "Sections retrieved successfully for semester");
    }

    @GetMapping("/course")
    public ResponseEntity<ApiResponse<List<Section>>> getSectionByCourse(
            @RequestParam int courseId ) {
        logger.info("Fetching sections for semester id: {}", courseId);
        long id = courseId;
        validateId(id);
        List<Section> sections = sectionService.getSectionsByCourseId(courseId);
        return success(sections, "Sections retrieved successfully for course");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Section>> getSectionById(@PathVariable Long id) {
        logger.info("Fetching section with id: {}", id);
        validateId(id);
        Section section = sectionService.findById(id);
        return success(section, "Section retrieved successfully");
    }

    @PostMapping
    public ResponseEntity<?> createSection(@RequestBody SectionDTO section) {
        logger.info("Creating new section: {}", section);
        Section createdSection = sectionService.createSection(
                section.getSectionNumber(),
                section.getCourseId(),
                section.getSemesterId()
        );
        return success(createdSection, "Section created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSection(@PathVariable Long id, @RequestBody SectionDTO section) {
        logger.info("Updating section with id {}: {}", id, section);
        validateId(id);
        Section updated = sectionService.updateSection(
                id,
                section.getSectionNumber(),
                section.getCourseId(),
                section.getSemesterId()
        );
        return success(updated, "Section updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSection(@PathVariable Long id) {
        logger.info("Deleting section with id: {}", id);
        validateId(id);
        sectionService.removeSection(id);
        return success(null, "Section deleted successfully");
    }

}
