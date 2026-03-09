package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.entity.CourseIndicator;
import com.abetappteam.abetapp.entity.Requests.CourseIndicator.CourseIndicatorSearchRequest;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.repository.CourseIndicatorRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CourseIndicatorService {

    private static final Logger logger = LoggerFactory.getLogger(CourseIndicatorService.class);

    @Autowired
    private CourseIndicatorRepository repository;

    @Transactional
    public CourseIndicator getOrCreate(Long courseId, Long indicatorId) {
        Optional<CourseIndicator> existing = repository.findByCourseIdAndIndicatorId(courseId, indicatorId);

        if (existing.isPresent()) {
            return existing.get();
        }

        CourseIndicator ci = new CourseIndicator();
        ci.setCourseId(courseId);
        ci.setIndicatorId(indicatorId);
        ci.setIsActive(true);

        return repository.save(ci);
    }

    // Find by ID
    @Transactional(readOnly = true)
    public CourseIndicator findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CourseIndicator not found with id: " + id));
    }

    // Search with filters
    @Transactional(readOnly = true)
    public List<CourseIndicator> searchCourseIndicators(CourseIndicatorSearchRequest request) {
        Long courseId = request.courseId() != null ? request.courseId().longValue() : null;
        Long indicatorId = request.indicatorId() != null ? request.indicatorId().longValue() : null;
        Boolean isActive = request.isActive();

        if (courseId != null && indicatorId != null && isActive != null) {
            return repository.findByCourseIdAndIndicatorIdAndIsActive(courseId, indicatorId, isActive)
                    .map(List::of)
                    .orElse(List.of());
        }

        if (courseId != null && isActive != null) {
            return repository.findByCourseIdAndIsActive(courseId, isActive);
        }

        if (indicatorId != null && isActive != null) {
            return repository.findByIndicatorIdAndIsActive(indicatorId, isActive);
        }

        if (courseId != null) {
            return repository.findByCourseId(courseId);
        }

        if (indicatorId != null) {
            return repository.findByIndicatorId(indicatorId);
        }

        if (isActive != null) {
            return repository.findByIsActive(isActive);
        }

        return repository.findAll();
    }

    // Get all active indicators for a course
    @Transactional(readOnly = true)
    public List<CourseIndicator> findActiveIndicatorsByCourseId(Long courseId) {
        logger.debug("Fetching active indicators for courseId: {}", courseId);
        return repository.findActiveIndicatorsByCourseId(courseId);
    }

    // Get all active courses for an indicator
    @Transactional(readOnly = true)
    public List<CourseIndicator> findActiveCoursesByIndicatorId(Long indicatorId) {
        logger.debug("Fetching active courses for indicatorId: {}", indicatorId);
        return repository.findActiveCoursesByIndicatorId(indicatorId);
    }

    // Activate a CourseIndicator
    @Transactional
    public CourseIndicator activate(Long id) {
        CourseIndicator ci = findById(id);
        ci.setIsActive(true);
        logger.info("Activating CourseIndicator: {}", id);
        return repository.save(ci);
    }

    // Deactivate a CourseIndicator
    @Transactional
    public CourseIndicator deactivate(Long id) {
        CourseIndicator ci = findById(id);
        ci.setIsActive(false);
        logger.info("Deactivating CourseIndicator: {}", id);
        return repository.save(ci);
    }

    // Check if a course has any active indicators
    @Transactional(readOnly = true)
    public boolean hasActiveIndicators(Long courseId) {
        return repository.hasActiveIndicators(courseId);
    }

    // Count active indicators for a course
    @Transactional(readOnly = true)
    public long countActiveIndicatorsByCourse(Long courseId) {
        return repository.countByCourseIdAndIsActive(courseId, true);
    }
}
