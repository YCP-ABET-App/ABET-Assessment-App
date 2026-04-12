package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.BaseRepositoryTest;
import com.abetappteam.abetapp.entity.Section;
import com.abetappteam.abetapp.entity.Requests.Section.SectionSearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SectionRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private SectionRepository sectionRepository;

    private Section section1;
    private Section section2;

    @BeforeEach
    void setUp() {
        section1 = createSection("001", 1, 1);
        section2 = createSection("002", 1, 1);
    }

    private Section createSection(String sectionNumber, int courseId, int semesterId) {
        Section s = new Section();
        s.setSectionNumber(sectionNumber);
        s.setCourseId(courseId);
        s.setSemesterId(semesterId);
        return s;
    }

    @Test
    void shouldSaveAndRetrieveSection() {
        Section saved = sectionRepository.save(section1);
        clearContext();

        Optional<Section> found = sectionRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSectionNumber()).isEqualTo("001");
        assertThat(found.get().getCourseId()).isEqualTo(1);
        assertThat(found.get().getSemesterId()).isEqualTo(1);
    }

    @Test
    void shouldFindByCourseId() {
        sectionRepository.save(section1);
        sectionRepository.save(section2);
        sectionRepository.save(createSection("003", 2, 1)); // different course

        List<Section> found = sectionRepository.findByCourseId(1);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Section::getCourseId).containsOnly(1);
    }

    @Test
    void shouldFindByCourseIdPaged() {
        sectionRepository.save(section1);
        sectionRepository.save(section2);

        Page<Section> page = sectionRepository.findByCourseId(1, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldCountByCourseId() {
        sectionRepository.save(section1);
        sectionRepository.save(section2);
        sectionRepository.save(createSection("003", 2, 1));

        long count = sectionRepository.countByCourseId(1);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldFindBySemesterId() {
        sectionRepository.save(section1);
        sectionRepository.save(createSection("003", 1, 2)); // different semester

        List<Section> found = sectionRepository.findBySemesterId(1);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getSectionNumber()).isEqualTo("001");
    }

    @Test
    void shouldFindBySemesterIdPaged() {
        sectionRepository.save(section1);
        sectionRepository.save(section2);

        Page<Section> page = sectionRepository.findBySemesterId(1, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldCountBySemesterId() {
        sectionRepository.save(section1);
        sectionRepository.save(section2);
        sectionRepository.save(createSection("003", 1, 2));

        long count = sectionRepository.countBySemesterId(1);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldReturnTrueWhenSectionExists() {
        sectionRepository.save(section1);

        boolean exists = sectionRepository
                .existsBySectionNumberAndSemesterIdAndCourseId("001", 1, 1);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSectionDoesNotExist() {
        boolean exists = sectionRepository
                .existsBySectionNumberAndSemesterIdAndCourseId("999", 1, 1);

        assertThat(exists).isFalse();
    }

    @Test
    void shouldSearchByCourseId() {
        sectionRepository.save(section1);
        sectionRepository.save(section2);
        sectionRepository.save(createSection("003", 2, 1));

        SectionSearchRequest request = new SectionSearchRequest(null, null, null, 1, null);
        List<Section> found = sectionRepository.searchSections(request);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Section::getCourseId).containsOnly(1);
    }

    @Test
    void shouldSearchBySemesterId() {
        sectionRepository.save(section1);
        sectionRepository.save(createSection("003", 1, 2));

        SectionSearchRequest request = new SectionSearchRequest(null, 1, null, null, null);
        List<Section> found = sectionRepository.searchSections(request);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getSemesterId()).isEqualTo(1);
    }

    @Test
    void shouldReturnAllSectionsWhenNoFilters() {
        sectionRepository.save(section1);
        sectionRepository.save(section2);

        SectionSearchRequest request = new SectionSearchRequest(null, null, null, null, null);
        List<Section> found = sectionRepository.searchSections(request);

        assertThat(found).hasSize(2);
    }
}