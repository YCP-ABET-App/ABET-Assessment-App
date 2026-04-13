package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.BaseRepositoryTest;
import com.abetappteam.abetapp.entity.SectionIndicator;
import com.abetappteam.abetapp.entity.Requests.SectionIndicator.SectionIndicatorRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SectionIndicatorRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private SectionIndicatorRepository sectionIndicatorRepository;

    private SectionIndicator si1;
    private SectionIndicator si2;

    @BeforeEach
    void setUp() {
        si1 = new SectionIndicator(1, 1, true);
        si2 = new SectionIndicator(1, 2, false);
    }

    @Test
    void shouldSaveAndRetrieveSectionIndicator() {
        SectionIndicator saved = sectionIndicatorRepository.save(si1);
        clearContext();

        Optional<SectionIndicator> found = sectionIndicatorRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSectionId()).isEqualTo(1);
        assertThat(found.get().getIndicatorId()).isEqualTo(1);
    }

    @Test
    void shouldSearchBySectionId() {
        sectionIndicatorRepository.save(si1);
        sectionIndicatorRepository.save(si2);
        sectionIndicatorRepository.save(new SectionIndicator(2, 1, true)); // different section

        SectionIndicatorRequest request = new SectionIndicatorRequest(null, List.of(1), null);
        List<SectionIndicator> found = sectionIndicatorRepository.searchSectionIndicators(
                request.ids(), request.sectionIds(), request.indicatorIds());

        assertThat(found).hasSize(2);
        assertThat(found).extracting(SectionIndicator::getSectionId).containsOnly(1);
    }

    @Test
    void shouldSearchByIndicatorId() {
        sectionIndicatorRepository.save(si1);
        sectionIndicatorRepository.save(si2);
        sectionIndicatorRepository.save(new SectionIndicator(2, 1, true)); // same indicator as si1

        SectionIndicatorRequest request = new SectionIndicatorRequest(null, null, List.of(1));
        List<SectionIndicator> found = sectionIndicatorRepository.searchSectionIndicators(
                request.ids(), request.sectionIds(), request.indicatorIds());

        assertThat(found).hasSize(2);
        assertThat(found).extracting(SectionIndicator::getIndicatorId).containsOnly(1);
    }

    @Test
    void shouldSearchByIds() {
        SectionIndicator saved1 = sectionIndicatorRepository.save(si1);
        sectionIndicatorRepository.save(si2);

        SectionIndicatorRequest request = new SectionIndicatorRequest(
                List.of(saved1.getId().intValue()), null, null);
        List<SectionIndicator> found = sectionIndicatorRepository.searchSectionIndicators(
                request.ids(), request.sectionIds(), request.indicatorIds());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(saved1.getId());
    }

    @Test
    void shouldSearchByMultipleSectionIds() {
        sectionIndicatorRepository.save(si1);
        sectionIndicatorRepository.save(si2);
        sectionIndicatorRepository.save(new SectionIndicator(2, 3, true));
        sectionIndicatorRepository.save(new SectionIndicator(3, 1, true));

        SectionIndicatorRequest request = new SectionIndicatorRequest(null, List.of(1, 2), null);
        List<SectionIndicator> found = sectionIndicatorRepository.searchSectionIndicators(
                request.ids(), request.sectionIds(), request.indicatorIds());

        assertThat(found).hasSize(3);
        assertThat(found).extracting(SectionIndicator::getSectionId).containsOnly(1, 2);
    }

    @Test
    void shouldReturnAllWhenNoFilters() {
        sectionIndicatorRepository.save(si1);
        sectionIndicatorRepository.save(si2);

        SectionIndicatorRequest request = new SectionIndicatorRequest(null, null, null);
        List<SectionIndicator> found = sectionIndicatorRepository.searchSectionIndicators(
                request.ids(), request.sectionIds(), request.indicatorIds());

        assertThat(found).hasSize(2);
    }
}