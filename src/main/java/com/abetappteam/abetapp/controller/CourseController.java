package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.dto.CourseDTO;
import com.abetappteam.abetapp.dto.PagedResponse;
import com.abetappteam.abetapp.entity.Course;
import com.abetappteam.abetapp.entity.CourseIndicator;
import com.abetappteam.abetapp.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for course entity operations
 * Manages courses and measure completeness
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController extends BaseController {

    @Autowired
    private CourseService courseService;



    /**
     * Create a new course
     */
    @PostMapping
    public ResponseEntity<?> createCourse(
            @Valid @RequestBody CourseDTO dto,
            BindingResult result) {

        if (result.hasErrors()) {
            return validationError(result);
        }

        logger.info("Creating new course: {} ({})", dto.getCourseName(), dto.getCourseCode());
        Course course = courseService.createCourse(dto);
        return created(course);
    }

    /**
     * Update an existing course
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseDTO dto) {

        logger.info("Updating course with ID: {}", id);
        validateId(id);
        Course updated = courseService.updateCourse(id, dto);
        return success(updated, "Course updated successfully");
    }

    /**
     * Update student count for a course
     */
    @PutMapping("/{id}/student-count")
    public ResponseEntity<ApiResponse<Course>> updateStudentCount(
            @PathVariable Long id,
            @RequestBody UpdateStudentCountRequest request) {

        logger.info("Updating student count for course with ID: {}", id);
        validateId(id);
        Course updated = courseService.updateStudentCount(id, request.getStudentCount());
        return success(updated, "Student count updated successfully");
    }

    /**
     * Remove/delete a course
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeCourse(@PathVariable Long id) {
        logger.info("Removing course with ID: {}", id);
        validateId(id);
        courseService.removeCourse(id);
        return success(null, "Course removed successfully");
    }

    /**
     * Deactivate a course
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Course>> deactivateCourse(@PathVariable Long id) {
        logger.info("Deactivating course with ID: {}", id);
        validateId(id);
        courseService.deactivateCourse(id);
        Course course = courseService.findById(id);
        return success(course, "Course deactivated successfully");
    }

    /**
     * Activate a course
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Course>> activateCourse(@PathVariable Long id) {
        logger.info("Activating course with ID: {}", id);
        validateId(id);
        courseService.activateCourse(id);
        Course course = courseService.findById(id);
        return success(course, "Course activated successfully");
    }


    // Instructor assignments
    @PostMapping("/{courseId}/instructors/{programUserId}")
    public ResponseEntity<Void> assignInstructor(
            @PathVariable Long courseId,
            @PathVariable Long programUserId) {
        courseService.assignInstructor(courseId, programUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{courseId}/instructors/{programUserId}")
    public ResponseEntity<Void> removeInstructor(
            @PathVariable Long courseId,
            @PathVariable Long programUserId) {
        courseService.removeInstructor(courseId, programUserId);
        return ResponseEntity.ok().build();
    }

    // Indicator assignments
    @PostMapping("/{courseId}/indicators/{indicatorId}")
    public ResponseEntity<Void> assignIndicator(
            @PathVariable Long courseId,
            @PathVariable Long indicatorId) {
        courseService.assignIndicator(courseId, indicatorId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{courseId}/indicators/{indicatorId}")
    public ResponseEntity<Void> removeIndicator(
            @PathVariable Long courseId,
            @PathVariable Long indicatorId) {
        courseService.removeIndicator(courseId, indicatorId);
        return ResponseEntity.ok().build();
    }

    /**
     * Request DTO for updating student count
     */
    public static class UpdateStudentCountRequest {
        private Integer studentCount;

        public Integer getStudentCount() {
            return studentCount;
        }

        public void setStudentCount(Integer studentCount) {
            this.studentCount = studentCount;
        }
    }
}