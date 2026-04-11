package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.dto.CourseDTO;
import com.abetappteam.abetapp.entity.*;
import com.abetappteam.abetapp.entity.Requests.Course.CourseSearchRequest;
import com.abetappteam.abetapp.exception.BusinessException;
import com.abetappteam.abetapp.exception.ConflictException;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.repository.*;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Course entity
 */
@Service
public class CourseService extends BaseService<Course, Long, CourseRepository> {

    @Autowired
    public CourseService(CourseRepository repository, CourseIndicatorRepository courseIndicatorRepository) {
        super(repository);
    }

    @Autowired
    private CourseInstructorRepository courseInstructorRepository;

    @Autowired
    private CourseIndicatorRepository courseIndicatorRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private MeasureRepository measureRepository;

    @Autowired
    private MeasureResultRepository measureResultRepository;

    @Autowired
    private ScheduleEntryRepository scheduleEntryRepository;

    @Override
    protected String getEntityName() {
        return "Course";
    }

    @Transactional
    public Course createCourse(String courseCode, String courseName, String courseDescription, Integer studentCount) {
        // Check for duplicate course code in the same semester
        if (repository.existsByCourseCodeIgnoreCase(courseCode)) {
            throw new ConflictException("Course with code '" + courseCode + "' already exists in this semester");
        }

        Course course = new Course();
        course.setCourseCode(courseCode);
        course.setCourseName(courseName);
        course.setCourseDescription(courseDescription);
        course.setStudentCount(studentCount);
        course.setIsActive(true);

        logger.info("Creating new course: {} - {}", courseCode, courseName);
        return repository.save(course);
    }

    @Transactional
    public Course createCourse(CourseDTO dto) {
        return createCourse(dto.getCourseCode(), dto.getCourseName(),
                dto.getCourseDescription(), dto.getStudentCount());
    }

    @Transactional(readOnly = true)
    public List<Course> searchCourse(CourseSearchRequest request) {
        logger.info("Service: searching courses with request: {}", request);
        List<Course> result = repository.searchCourse(
                request.id(),
                request.courseCode(),
                request.courseName(),
                request.courseDescription(),
                request.student_count(),
                request.mirrorId(),
                request.isActive());

        System.out.print("Resulting Courses: " + result);
        return result;
    }

    @Transactional
    public Course updateCourse(Long courseId, String courseCode, String courseName, String courseDescription,
            Integer studentCount) {
        Course course = findById(courseId);

        // Check for duplicate course code if it's being changed
        if (courseCode != null && !courseCode.equals(course.getCourseCode())) {
            if (repository.existsByCourseCodeIgnoreCase(courseCode)) {
                throw new ConflictException("Course with code '" + courseCode + "' already exists in this semester");
            }
            course.setCourseCode(courseCode);
        }

        if (courseName != null) {
            course.setCourseName(courseName);
        }
        if (courseDescription != null) {
            course.setCourseDescription(courseDescription);
        }
        if (studentCount != null) {
            course.setStudentCount(studentCount);
        }

        logger.info("Updating course: {}", courseId);
        return repository.save(course);
    }

    @Transactional
    public Course updateCourse(Long courseId, CourseDTO dto) {
        return updateCourse(courseId, dto.getCourseCode(), dto.getCourseName(),
                dto.getCourseDescription(), dto.getStudentCount());
    }

    @Transactional
    public Course versionCourse(Long courseId, CourseDTO dto) {
        Course original = findById(courseId);

        String newCode = dto.getCourseCode() != null ? dto.getCourseCode() : original.getCourseCode();
        if (repository.existsByCourseCodeIgnoreCase(newCode)) {
            throw new ConflictException("Course with code '" + newCode + "' already exists in this semester");
        }

        Course version = new Course();
        version.setCourseCode(newCode);
        version.setCourseName(dto.getCourseName() != null ? dto.getCourseName() : original.getCourseName());
        version.setCourseDescription(
                dto.getCourseDescription() != null ? dto.getCourseDescription() : original.getCourseDescription());
        version.setStudentCount(dto.getStudentCount() != null ? dto.getStudentCount() : original.getStudentCount());
        version.setIsActive(true);

        original.setIsActive(false);
        repository.save(original);

        logger.info("Versioning course {}: creating new version with code {}", courseId, newCode);
        return repository.save(version);
    }

    @Transactional
    public Course updateStudentCount(Long courseId, Integer studentCount) {
        Course course = findById(courseId);
        course.setStudentCount(studentCount);
        logger.info("Updating student count for course {}: {}", courseId, studentCount);
        return repository.save(course);
    }

    @Transactional
    public void removeCourse(Long courseId) {
        Course course = findById(courseId);
        int cIdInt = courseId.intValue();

        if (repository.countMeasuresInReviewByCourseId(courseId) > 0) {
            throw new BusinessException("Cannot delete course with measures submitted for review");
        }

        sectionRepository.deleteSectionProgramsByCourseId(cIdInt);
        sectionRepository.deleteSectionUsersByCourseId(cIdInt);
        sectionRepository.deleteByCourseId(courseId.intValue());

        List<ScheduleEntry> entries = scheduleEntryRepository.findByCourseId(cIdInt);

        for (ScheduleEntry se : entries) {

            List<Measure> measures = measureRepository.findByScheduleEntryId(se.getId());

            for (Measure m : measures) {
                measureResultRepository.deleteByMeasureId(m.getId());
            }

            measureRepository.deleteByScheduleEntryId(se.getId());
        }

        // Delete schedule entries
        // scheduleEntryRepository.deleteByCourseId(courseId);

        courseIndicatorRepository.deleteByCourseId(courseId);
        courseInstructorRepository.deleteByCourseId(courseId);
        logger.info("Removing course with cascade: {} - {}", course.getCourseCode(), course.getCourseName());
        repository.delete(course);
    }

    @Transactional
    public void deactivateCourse(Long courseId) {
        Course course = findById(courseId);
        course.setIsActive(false);
        logger.info("Deactivating course: {} - {}", course.getCourseCode(), course.getCourseName());
        repository.save(course);
    }

    @Transactional
    public void activateCourse(Long courseId) {
        Course course = findById(courseId);
        course.setIsActive(true);
        logger.info("Activating course: {} - {}", course.getCourseCode(), course.getCourseName());
        repository.save(course);
    }

    @Transactional(readOnly = true)
    public List<Course> getActiveCoursesByProgramUserId(Long programUserId) {
        logger.debug("Fetching active courses for program user ID: {}", programUserId);
        return repository.findActiveCoursesByProgramUserId(programUserId);
    }

    @Transactional(readOnly = true)
    public MeasureCompletenessResponse calculateMeasureCompleteness(Long courseId) {
        Course course = findById(courseId);

        int totalMeasures = getTotalMeasuresForCourse(courseId);
        int completedMeasures = getCompletedMeasuresForCourse(courseId);
        int inProgressMeasures = getInProgressMeasuresForCourse(courseId);
        int submittedMeasures = getSubmittedMeasuresForCourse(courseId);

        double completionPercentage = totalMeasures > 0 ? (double) completedMeasures / totalMeasures * 100 : 0;

        MeasureCompletenessResponse response = new MeasureCompletenessResponse();
        response.setCourseId(courseId);
        response.setTotalMeasures(totalMeasures);
        response.setCompletedMeasures(completedMeasures);
        response.setInProgressMeasures(inProgressMeasures);
        response.setSubmittedMeasures(submittedMeasures);
        response.setCompletionPercentage(completionPercentage);

        return response;
    }

    @Transactional(readOnly = true)
    public Course findByCourseCode(String courseCode) {
        return repository.findByCourseCodeIgnoreCase(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with code: " + courseCode));
    }

    @Transactional(readOnly = true)
    public boolean existsByCourseCode(String courseCode) {
        return repository.existsByCourseCodeIgnoreCase(courseCode);
    }

    @Transactional
    public void assignInstructor(Long courseId, Long programUserId) {
        Course course = findById(courseId); // validates course exists

        if (courseInstructorRepository.existsByCourseIdAndProgramUserId(courseId, programUserId)) {
            throw new ConflictException("Instructor already assigned to this course");
        }

        CourseInstructor assignment = new CourseInstructor(programUserId, courseId);
        courseInstructorRepository.save(assignment);
        logger.info("Assigned instructor {} to course {}", programUserId, courseId);
    }

    @Transactional
    public void removeInstructor(Long courseId, Long programUserId) {
        CourseInstructor assignment = courseInstructorRepository
                .findByCourseIdAndProgramUserId(courseId, programUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor assignment not found"));

        assignment.setIsActive(false);
        courseInstructorRepository.save(assignment);
        logger.info("Removed instructor {} from course {}", programUserId, courseId);
    }

    @Transactional(readOnly = true)
    public List<Long> getInstructorIds(Long courseId) {
        return courseInstructorRepository.findByCourseIdAndIsActive(courseId, true)
                .stream()
                .map(CourseInstructor::getProgramUserId)
                .collect(Collectors.toList());
    }

    // Indicator management
    @Transactional
    public void assignIndicator(Long courseId, Long indicatorId) {
        Course course = findById(courseId);

        if (courseIndicatorRepository.existsByCourseIdAndIndicatorId(courseId, indicatorId)) {
            throw new ConflictException("Indicator already assigned to this course");
        }

        CourseIndicator assignment = new CourseIndicator(courseId, indicatorId);
        courseIndicatorRepository.save(assignment);
        logger.info("Assigned indicator {} to course {}", indicatorId, courseId);
    }

    @Transactional
    public void removeIndicator(Long courseId, Long indicatorId) {
        CourseIndicator assignment = courseIndicatorRepository
                .findByCourseIdAndIndicatorId(courseId, indicatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator assignment not found"));

        assignment.setIsActive(false);
        courseIndicatorRepository.save(assignment);
        logger.info("Removed indicator {} from course {}", indicatorId, courseId);
    }

    @Transactional(readOnly = true)
    public List<Long> getIndicatorIds(Long courseId) {
        return courseIndicatorRepository.findByCourseIdAndIsActive(courseId, true)
                .stream()
                .map(CourseIndicator::getIndicatorId)
                .collect(Collectors.toList());
    }

    // Return CourseIndicator
    @Transactional(readOnly = true)
    public Optional<CourseIndicator> getCourseIndicatorByCourseIdAndIndicatorId(Long courseId, Long indicatorId) {
        return courseIndicatorRepository.findByCourseIdAndIndicatorId(courseId, indicatorId);
    }

    @Transactional(readOnly = true)
    public Optional<CourseIndicator> getCourseIndicatorById(Long id) {
        return courseIndicatorRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Course> getAllActiveCourses() {
        logger.debug("Fetching all active courses");
        return repository.findByIsActive(true);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveCourses(Long programUserId) {
        logger.debug("Checking if program user {} has active courses", programUserId);
        return courseInstructorRepository.existsByProgramUserIdAndIsActive(programUserId, true);
    }

    @Transactional(readOnly = true)
    public List<Course> getActiveCoursesByProgramUserIdAndSemester(Long programUserId, Long semesterId) {
        logger.info("Getting active courses for program user ID: {} and semester ID: {}",
                programUserId, semesterId);
        return courseInstructorRepository.findActiveCoursesByProgramUserIdAndSemesterId(
                programUserId, semesterId);
    }

    // Helper methods for business logic
    private boolean hasMeasuresInReview(Long courseId) {
        return repository.countMeasuresInReviewByCourseId(courseId) > 0;
    }

    private int getTotalMeasuresForCourse(Long courseId) {
        return repository.countTotalMeasuresByCourseId(courseId);
    }

    private int getCompletedMeasuresForCourse(Long courseId) {
        return repository.countCompletedMeasuresByCourseId(courseId);
    }

    private int getInProgressMeasuresForCourse(Long courseId) {
        return repository.countInProgressMeasuresByCourseId(courseId);
    }

    private int getSubmittedMeasuresForCourse(Long courseId) {
        return repository.countSubmittedMeasuresByCourseId(courseId);
    }

    /**
     * Response DTO for measure completeness
     */
    public static class MeasureCompletenessResponse {
        private Long courseId;
        private int totalMeasures;
        private int completedMeasures;
        private int inProgressMeasures;
        private int submittedMeasures;
        private double completionPercentage;

        // Getters and setters
        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }

        public int getTotalMeasures() {
            return totalMeasures;
        }

        public void setTotalMeasures(int totalMeasures) {
            this.totalMeasures = totalMeasures;
        }

        public int getCompletedMeasures() {
            return completedMeasures;
        }

        public void setCompletedMeasures(int completedMeasures) {
            this.completedMeasures = completedMeasures;
        }

        public int getInProgressMeasures() {
            return inProgressMeasures;
        }

        public void setInProgressMeasures(int inProgressMeasures) {
            this.inProgressMeasures = inProgressMeasures;
        }

        public int getSubmittedMeasures() {
            return submittedMeasures;
        }

        public void setSubmittedMeasures(int submittedMeasures) {
            this.submittedMeasures = submittedMeasures;
        }

        public double getCompletionPercentage() {
            return completionPercentage;
        }

        public void setCompletionPercentage(double completionPercentage) {
            this.completionPercentage = completionPercentage;
        }
    }
}