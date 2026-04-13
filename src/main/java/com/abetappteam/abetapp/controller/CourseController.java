package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.dto.CourseDTO;
import com.abetappteam.abetapp.entity.Course;
import com.abetappteam.abetapp.entity.Requests.Course.CourseSearchRequest;
import com.abetappteam.abetapp.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

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
     * Search courses by name or course code
     */
    @GetMapping("/searchCourse")
    public ResponseEntity<ApiResponse<List<Course>>> searchCourse(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String courseDescription,
            @RequestParam(required = false) Integer student_count,
            @RequestParam(required = false) Integer mirrorId,
            @RequestParam(required = false) Boolean isActive) {
        CourseSearchRequest request = new CourseSearchRequest(id, courseCode, courseName, courseDescription,
                student_count, mirrorId, isActive);

        logger.info("Search request received for: {}", request);

        List<Course> courses = courseService.searchCourse(request);

        return success(courses, "Courses retrieved successfully");
    }

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

    /**
     * Get active courses, optionally filtered by program.
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Course>>> getActiveCourses(
            @RequestParam(required = false) Long programId) {

        List<Course> courses;
        if (programId != null) {
            logger.info("Fetching active courses for programId: {}", programId);
            courses = courseService.getActiveCoursesByProgramId(programId);
        } else {
            logger.info("Fetching all active courses");
            courses = courseService.getAllActiveCourses();
        }
        return success(courses, "Active courses retrieved successfully");
    }

    /**
     * Get indicator IDs assigned to a course.
     */
    @GetMapping("/{courseId}/indicators")
    public ResponseEntity<List<Long>> getIndicatorIds(@PathVariable Long courseId) {
        logger.info("Fetching indicator IDs for courseId: {}", courseId);
        validateId(courseId);
        List<Long> ids = courseService.getIndicatorIds(courseId);
        return ResponseEntity.ok(ids);
    }

    /**
     * Get all soft-deleted courses
     */
    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<List<Course>>> getDeletedCourses() {
        logger.info("Fetching all soft-deleted courses");
        List<Course> deleted = courseService.findDeletedCourses();
        return success(deleted, "Deleted courses retrieved successfully");
    }

    /**
     * Permanently delete a soft-deleted course
     */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> permanentDeleteCourse(@PathVariable Long id) {
        logger.info("Permanently deleting course with ID: {}", id);
        validateId(id);
        courseService.permanentDeleteCourse(id);
        return success(null, "Course permanently deleted");
    }

    /**
     * Create a new version of a course (preserves original as inactive)
     */
    @PostMapping("/{id}/version")
    public ResponseEntity<ApiResponse<Course>> versionCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseDTO dto) {

        logger.info("Versioning course with ID: {}", id);
        validateId(id);
        Course versioned = courseService.versionCourse(id, dto);
        return created(versioned);
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