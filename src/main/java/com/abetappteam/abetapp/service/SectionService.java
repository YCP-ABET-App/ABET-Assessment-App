package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.dto.SectionDTO;
import com.abetappteam.abetapp.entity.Course;
import com.abetappteam.abetapp.entity.Section;
import com.abetappteam.abetapp.exception.BusinessException;
import com.abetappteam.abetapp.exception.ConflictException;
import com.abetappteam.abetapp.repository.CourseRepository;
import com.abetappteam.abetapp.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for Section entity
 */
@Service
public class SectionService extends BaseService<Section, Long, SectionRepository> {

    private final CourseRepository courseRepository;

    @Autowired
    public SectionService(SectionRepository repository,
                          CourseRepository courseRepository) {
        super(repository);
        this.courseRepository = courseRepository;
    }

    @Override
    protected String getEntityName() {
        return "Section";
    }

    // ==============================
    // CREATE
    // ==============================

    @Transactional
    public Section createSection(SectionDTO dto) {

        // Validate course exists
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() ->
                        new BusinessException("Course not found with ID: " + dto.getCourseId()));

        // Enforce semester consistency
        if (!course.getSemesterId().equals(dto.getSemesterId())) {
            throw new BusinessException(
                    "Section semester must match course semester");
        }

        // Check duplicate section
        if (repository.existsByNameIgnoreCaseAndCourseIdAndSemesterId(
                dto.getName(),
                dto.getCourseId(),
                dto.getSemesterId())) {

            throw new ConflictException(
                    "Section '" + dto.getName() + "' already exists for this course in this semester");
        }

        Section section = new Section();
        section.setName(dto.getName());
        section.setCourseId(dto.getCourseId());
        section.setInstructorId(dto.getInstructorId());
        section.setSemesterId(dto.getSemesterId());

        logger.info("Creating section: {} for course {}", dto.getName(), dto.getCourseId());
        return repository.save(section);
    }

    // ==============================
    // UPDATE
    // ==============================

    @Transactional
    public Section updateSection(Long sectionId, SectionDTO dto) {

        Section section = findById(sectionId);

        if (dto.getName() != null) {
            section.setName(dto.getName());
        }

        if (dto.getInstructorId() != null) {
            section.setInstructorId(dto.getInstructorId());
        }

        logger.info("Updating section: {}", sectionId);
        return repository.save(section);
    }

    // ==============================
    // DELETE
    // ==============================

    @Transactional
    public void removeSection(Long sectionId) {
        Section section = findById(sectionId);

        logger.info("Removing section: {}", section.getName());
        repository.delete(section);
    }

    // ==============================
    // READ METHODS
    // ==============================

    @Transactional(readOnly = true)
    public Page<Section> getSectionsBySemester(Long semesterId, Pageable pageable) {
        return repository.findBySemesterId(semesterId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Section> getSectionsByCourse(Long courseId) {
        return repository.findByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public List<Section> getSectionsByInstructor(Long instructorId) {
        return repository.findByInstructorId(instructorId);
    }

    @Transactional(readOnly = true)
    public boolean existsByNameAndCourseAndSemester(String name, Long courseId, Long semesterId) {
        return repository.existsByNameIgnoreCaseAndCourseIdAndSemesterId(
                name, courseId, semesterId);
    }

}