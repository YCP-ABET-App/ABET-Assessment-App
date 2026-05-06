package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.BaseRepositoryTest;
import com.abetappteam.abetapp.entity.SectionProgram;
import com.abetappteam.abetapp.entity.Requests.SectionProgram.SectionProgramSearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SectionProgramRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private SectionProgramRepository sectionProgramRepository;

    private SectionProgram sp1;
    private SectionProgram sp2;

    @BeforeEach
    void setUp() {
        sp1 = new SectionProgram(1, 1);
        sp2 = new SectionProgram(1, 2);
    }

    @Test
    void shouldSaveAndRetrieveSectionProgram() {
        SectionProgram saved = sectionProgramRepository.save(sp1);
        clearContext();

        Optional<SectionProgram> found = sectionProgramRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSectionId()).isEqualTo(1);
        assertThat(found.get().getProgramId()).isEqualTo(1);
    }

    @Test
    void shouldFindBySectionId() {
        sectionProgramRepository.save(sp1);
        sectionProgramRepository.save(sp2);
        sectionProgramRepository.save(new SectionProgram(2, 1)); // different section

        List<SectionProgram> found = sectionProgramRepository.findBySectionId(1);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(SectionProgram::getSectionId).containsOnly(1);
    }

    @Test
    void shouldFindBySectionIdPaged() {
        sectionProgramRepository.save(sp1);
        sectionProgramRepository.save(sp2);

        Page<SectionProgram> page = sectionProgramRepository.findBySectionId(1, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldCountBySectionId() {
        sectionProgramRepository.save(sp1);
        sectionProgramRepository.save(sp2);
        sectionProgramRepository.save(new SectionProgram(2, 1));

        long count = sectionProgramRepository.countBySectionId(1);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldFindByProgramId() {
        sectionProgramRepository.save(sp1);
        sectionProgramRepository.save(new SectionProgram(2, 2)); // different program

        List<SectionProgram> found = sectionProgramRepository.findByProgramId(1);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getProgramId()).isEqualTo(1);
    }

    @Test
    void shouldFindByProgramIdPaged() {
        sectionProgramRepository.save(sp1);
        sectionProgramRepository.save(new SectionProgram(2, 1));

        Page<SectionProgram> page = sectionProgramRepository.findByProgramId(1, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldCountByProgramId() {
        sectionProgramRepository.save(sp1);
        sectionProgramRepository.save(new SectionProgram(2, 1));
        sectionProgramRepository.save(new SectionProgram(1, 2));

        long count = sectionProgramRepository.countByProgramId(1);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldReturnTrueWhenAssociationExists() {
        sectionProgramRepository.save(sp1);

        Boolean exists = sectionProgramRepository.existsBySectionIdAndProgramId(1, 1);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAssociationDoesNotExist() {
        Boolean exists = sectionProgramRepository.existsBySectionIdAndProgramId(99, 99);

        assertThat(exists).isFalse();
    }

    @Test
    void shouldSearchBySectionId() {
        sectionProgramRepository.save(sp1);
        sectionProgramRepository.save(sp2);
        sectionProgramRepository.save(new SectionProgram(2, 3));

        SectionProgramSearchRequest request = new SectionProgramSearchRequest(null, 1, null);
        List<SectionProgram> found = sectionProgramRepository.searchSectionProgram(request);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(SectionProgram::getSectionId).containsOnly(1);
    }

    @Test
    void shouldSearchByProgramId() {
        sectionProgramRepository.save(sp1);
        sectionProgramRepository.save(new SectionProgram(2, 2));

        SectionProgramSearchRequest request = new SectionProgramSearchRequest(null, null, 1);
        List<SectionProgram> found = sectionProgramRepository.searchSectionProgram(request);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getProgramId()).isEqualTo(1);
    }

    @Test
    void shouldSearchById() {
        SectionProgram saved = sectionProgramRepository.save(sp1);
        sectionProgramRepository.save(sp2);

        SectionProgramSearchRequest request = new SectionProgramSearchRequest(saved.getId().intValue(), null, null);
        List<SectionProgram> found = sectionProgramRepository.searchSectionProgram(request);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(saved.getId());
    }

    @Test
    void shouldReturnAllWhenNoFilters() {
        sectionProgramRepository.save(sp1);
        sectionProgramRepository.save(sp2);

        SectionProgramSearchRequest request = new SectionProgramSearchRequest(null, null, null);
        List<SectionProgram> found = sectionProgramRepository.searchSectionProgram(request);

        assertThat(found).hasSize(2);
    }
}