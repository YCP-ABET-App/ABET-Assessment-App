package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.dto.SectionDTO;
import com.abetappteam.abetapp.entity.Course;
import com.abetappteam.abetapp.entity.Requests.Course.CourseSearchRequest;
import com.abetappteam.abetapp.entity.Requests.Section.SectionSearchRequest;
import com.abetappteam.abetapp.entity.Requests.Section.SectionSearchResponse;
import com.abetappteam.abetapp.entity.Section;
import com.abetappteam.abetapp.service.CourseService;
import com.abetappteam.abetapp.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * Controller for section entity operations
 * Manages section
 */
@RestController
@RequestMapping("/api/section")
public class SectionController extends BaseController {

    @Autowired
    private SectionService sectionService;
    @Autowired
    private CourseService courseService;

    @GetMapping("/section")
    public ResponseEntity<ApiResponse<SectionSearchResponse>> searchSection(
            @RequestParam SectionSearchRequest body) {
        logger.info("Fetching sections for request: {}", body);
        List<Section> sections = sectionService.searchSections(body);

        List<CourseSearchRequest> requests = new ArrayList<>();
        List<Course> course = new ArrayList<>();

        for(Section section : sections) {
            requests.add(
                new CourseSearchRequest(
                    section.getCourseId(),
                    null,
                    null,
                    null,
                    -1,
                    -1,
                    true
                )
            );
        }

        List<Course> courses = new ArrayList<>();

        for(CourseSearchRequest req : requests) {
            List<Course> results = courseService.searchCourse(req);
            courses.addAll(results);
        }

        SectionSearchResponse response = new SectionSearchResponse(sections, courses);

        return success(response, "Sections retrieved successfully for course");
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
