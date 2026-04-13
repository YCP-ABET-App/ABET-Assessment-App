package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.BaseRepositoryTest;
import com.abetappteam.abetapp.entity.SectionUser;
import com.abetappteam.abetapp.entity.Requests.SectionUser.SectionUserSearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SectionUserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private SectionUserRepository sectionUserRepository;

    private SectionUser su1;
    private SectionUser su2;

    @BeforeEach
    void setUp() {
        su1 = new SectionUser(1, 1);
        su2 = new SectionUser(1, 2);
    }

    @Test
    void shouldSaveAndRetrieveSectionUser() {
        SectionUser saved = sectionUserRepository.save(su1);
        clearContext();

        Optional<SectionUser> found = sectionUserRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSectionId()).isEqualTo(1);
        assertThat(found.get().getUserId()).isEqualTo(1);
    }

    @Test
    void shouldFindBySectionId() {
        sectionUserRepository.save(su1);
        sectionUserRepository.save(su2);
        sectionUserRepository.save(new SectionUser(2, 1)); // different section

        List<SectionUser> found = sectionUserRepository.findBySectionId(1);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(SectionUser::getSectionId).containsOnly(1);
    }

    @Test
    void shouldCountBySectionId() {
        sectionUserRepository.save(su1);
        sectionUserRepository.save(su2);
        sectionUserRepository.save(new SectionUser(2, 3));

        long count = sectionUserRepository.countBySectionId(1);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldFindByUserId() {
        sectionUserRepository.save(su1);
        sectionUserRepository.save(new SectionUser(2, 1)); // same user, different section
        sectionUserRepository.save(new SectionUser(1, 3)); // different user

        List<SectionUser> found = sectionUserRepository.findByUserId(1);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(SectionUser::getUserId).containsOnly(1);
    }

    @Test
    void shouldCountByUserId() {
        sectionUserRepository.save(su1);
        sectionUserRepository.save(new SectionUser(2, 1));
        sectionUserRepository.save(new SectionUser(1, 2));

        long count = sectionUserRepository.countByUserId(1);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldReturnTrueWhenAssociationExists() {
        sectionUserRepository.save(su1);

        boolean exists = sectionUserRepository.existsBySectionIdAndUserId(1, 1);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAssociationDoesNotExist() {
        boolean exists = sectionUserRepository.existsBySectionIdAndUserId(99, 99);

        assertThat(exists).isFalse();
    }

    @Test
    void shouldSearchBySectionId() {
        sectionUserRepository.save(su1);
        sectionUserRepository.save(su2);
        sectionUserRepository.save(new SectionUser(2, 3));

        SectionUserSearchRequest request = new SectionUserSearchRequest(null, 1, null);
        List<SectionUser> found = sectionUserRepository.searchSectionUser(request);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(SectionUser::getSectionId).containsOnly(1);
    }

    @Test
    void shouldSearchByUserId() {
        sectionUserRepository.save(su1);
        sectionUserRepository.save(new SectionUser(2, 1));
        sectionUserRepository.save(new SectionUser(1, 2));

        SectionUserSearchRequest request = new SectionUserSearchRequest(null, null, 1);
        List<SectionUser> found = sectionUserRepository.searchSectionUser(request);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(SectionUser::getUserId).containsOnly(1);
    }

    @Test
    void shouldSearchById() {
        SectionUser saved = sectionUserRepository.save(su1);
        sectionUserRepository.save(su2);

        SectionUserSearchRequest request = new SectionUserSearchRequest(saved.getId().intValue(), null, null);
        List<SectionUser> found = sectionUserRepository.searchSectionUser(request);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(saved.getId());
    }

    @Test
    void shouldReturnAllWhenNoFilters() {
        sectionUserRepository.save(su1);
        sectionUserRepository.save(su2);

        SectionUserSearchRequest request = new SectionUserSearchRequest(null, null, null);
        List<SectionUser> found = sectionUserRepository.searchSectionUser(request);

        assertThat(found).hasSize(2);
    }
}