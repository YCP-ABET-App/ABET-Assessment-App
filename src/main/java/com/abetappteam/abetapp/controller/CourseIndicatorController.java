package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.entity.CourseIndicator;
import com.abetappteam.abetapp.entity.Requests.CourseIndicator.CourseIndicatorSearchRequest;
import com.abetappteam.abetapp.service.CourseIndicatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-indicators")
public class CourseIndicatorController extends BaseController {

    @Autowired
    private CourseIndicatorService courseIndicatorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseIndicator>>> searchCourseIndicators(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer indicatorId,
            @RequestParam(required = false) Boolean isActive) {

        CourseIndicatorSearchRequest request = new CourseIndicatorSearchRequest(courseId, indicatorId, isActive);
        logger.info("Search request received for course indicators: {}", request);

        List<CourseIndicator> results = courseIndicatorService.searchCourseIndicators(request);
        return success(results, "Course indicators retrieved successfully");
    }

    /**
     * Get a specific course indicator by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseIndicator>> getCourseIndicator(@PathVariable Long id) {
        logger.info("Fetching course indicator with ID: {}", id);
        validateId(id);
        CourseIndicator ci = courseIndicatorService.findById(id);
        return success(ci, "Course indicator retrieved successfully");
    }

    /**
     * Get all active indicators for a given course.
     */
    @GetMapping("/by-course/{courseId}")
    public ResponseEntity<ApiResponse<List<CourseIndicator>>> getActiveIndicatorsByCourse(
            @PathVariable Long courseId) {
        logger.info("Fetching active indicators for courseId: {}", courseId);
        validateId(courseId);
        List<CourseIndicator> results = courseIndicatorService.findActiveIndicatorsByCourseId(courseId);
        return success(results, "Active indicators retrieved successfully");
    }

    /**
     * Get all active courses for a given indicator.
     */
    @GetMapping("/by-indicator/{indicatorId}")
    public ResponseEntity<ApiResponse<List<CourseIndicator>>> getActiveCoursesByIndicator(
            @PathVariable Long indicatorId) {
        logger.info("Fetching active courses for indicatorId: {}", indicatorId);
        validateId(indicatorId);
        List<CourseIndicator> results = courseIndicatorService.findActiveCoursesByIndicatorId(indicatorId);
        return success(results, "Active courses retrieved successfully");
    }

    /**
     * Activate a course indicator relationship.
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<CourseIndicator>> activateCourseIndicator(@PathVariable Long id) {
        logger.info("Activating course indicator with ID: {}", id);
        validateId(id);
        CourseIndicator ci = courseIndicatorService.activate(id);
        return success(ci, "Course indicator activated successfully");
    }

    /**
     * Deactivate a course indicator relationship.
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<CourseIndicator>> deactivateCourseIndicator(@PathVariable Long id) {
        logger.info("Deactivating course indicator with ID: {}", id);
        validateId(id);
        CourseIndicator ci = courseIndicatorService.deactivate(id);
        return success(ci, "Course indicator deactivated successfully");
    }

    /**
     * Check if a course has any active indicators.
     */
    @GetMapping("/by-course/{courseId}/has-active")
    public ResponseEntity<ApiResponse<Boolean>> hasActiveIndicators(@PathVariable Long courseId) {
        logger.info("Checking active indicators for courseId: {}", courseId);
        validateId(courseId);
        boolean hasActive = courseIndicatorService.hasActiveIndicators(courseId);
        return success(hasActive, "Active indicator check completed");
    }

    /**
     * Count active indicators for a course.
     */
    @GetMapping("/by-course/{courseId}/count")
    public ResponseEntity<ApiResponse<Long>> countActiveIndicators(@PathVariable Long courseId) {
        logger.info("Counting active indicators for courseId: {}", courseId);
        validateId(courseId);
        long count = courseIndicatorService.countActiveIndicatorsByCourse(courseId);
        return success(count, "Active indicator count retrieved successfully");
    }
}