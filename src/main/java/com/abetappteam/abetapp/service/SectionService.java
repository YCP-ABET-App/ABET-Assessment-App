package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.entity.Course;
import com.abetappteam.abetapp.entity.Requests.Section.SectionSearchRequest;
import com.abetappteam.abetapp.entity.Section;
import com.abetappteam.abetapp.entity.SectionUser;
import com.abetappteam.abetapp.repository.SectionRepository;
import com.abetappteam.abetapp.repository.SectionUserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for Section entity
 */
@Service
public class SectionService extends BaseService<Section, Long, SectionRepository> {
    @Autowired
    public SectionService(SectionRepository repository) {
        super(repository);
    }

    @Autowired
    private SectionUserRepository sectionUserRepository;

    @Override
    protected String getEntityName() {
        return "Section";
    }

    @Transactional
    public Section createSection(String sectionNumber, int courseId, int semesterId) {
        // Check for duplicates
        if (repository.existsBySectionNumberAndSemesterIdAndCourseId(sectionNumber, semesterId, courseId)) {
            throw new IllegalArgumentException(
                    "A section with the same section number already exists for the given course and semester.");
        }

        Section section = new Section(sectionNumber, courseId, semesterId);
        logger.info("Creating new section: {}", section);
        Section result = repository.save(section);

        if(result != null) {
            logger.info("Section created successfully with id {}: {}", result.getId(), result);

            // TODO: In the future, we'll add performance indicators here

            return result;
        } else {
            logger.error("Failed to create section: {}", section);
            throw new RuntimeException("Failed to create section");
        }
    }

    @Transactional
    public Section updateSection(Long id, String sectionNumber, int courseId, int semesterId) {
        Section section = findById(id);

        // Check for a duplication section number for the same course and semester
        if (repository.existsBySectionNumberAndSemesterIdAndCourseId(sectionNumber, semesterId, courseId)) {
            throw new IllegalArgumentException(
                    "A section with the same section number already exists for the given course and semester.");
        }

        if (sectionNumber != null) {
            section.setSectionNumber(sectionNumber);
        }
        if (courseId != 0) {
            section.setCourseId(courseId);
        }
        if (semesterId != 0) {
            section.setSemesterId(semesterId);
        }

        logger.info("Updating section with id {}: {}", id, section);
        return repository.save(section);
    }

    @Transactional
    public void removeSection(Long id) {
        Section section = findById(id);
        logger.info("Removing section with id {}: {}", id, section);

        // Search for entries in section-user and remove them first to avoid foreign key constraint violations
        List<SectionUser> entries = sectionUserRepository.findBySectionId(Math.toIntExact(id));
        for(SectionUser entry : entries) {
            logger.info("Removing section-user entry with id {}: {}", entry.getId(), entry);
            sectionUserRepository.delete(entry);
        }

        repository.delete(section);
    }

    @Transactional(readOnly = true)
    public List<Section> searchSections(SectionSearchRequest request) {
        logger.info("Searching for sections with criteria: {}", request);
        return repository.searchSections(request);
    }

}
