package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.BaseServiceTest;
import com.abetappteam.abetapp.entity.Requests.SectionIndicator.SectionIndicatorRequest;
import com.abetappteam.abetapp.entity.SectionIndicator;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.repository.SectionIndicatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SectionIndicatorServiceTest extends BaseServiceTest {

    @Mock
    private SectionIndicatorRepository repository;

    @InjectMocks
    private SectionIndicatorService sectionIndicatorService;

    private SectionIndicator testSI;

    private static final int SECTION_ID = 5;
    private static final int INDICATOR_ID = 10;

    @BeforeEach
    void setUp() {
        testSI = new SectionIndicator(SECTION_ID, INDICATOR_ID, false);
        testSI.setId(1L);
    }

    // ─── createSectionIndicator ───────────────────────────────────────────────────

    @Test
    void createSectionIndicator_savesAndReturnsNewMapping() {
        when(repository.save(any(SectionIndicator.class))).thenReturn(testSI);

        SectionIndicator result = sectionIndicatorService.createSectionIndicator(SECTION_ID, INDICATOR_ID);

        assertThat(result).isNotNull();
        assertThat(result.getSectionId()).isEqualTo(SECTION_ID);
        assertThat(result.getIndicatorId()).isEqualTo(INDICATOR_ID);
        verify(repository).save(any(SectionIndicator.class));
    }

    @Test
    void createSectionIndicator_setsCorrectSectionAndIndicatorIds() {
        SectionIndicator captured = new SectionIndicator(7, 20, false);
        captured.setId(2L);
        when(repository.save(any(SectionIndicator.class))).thenReturn(captured);

        SectionIndicator result = sectionIndicatorService.createSectionIndicator(7, 20);

        assertThat(result.getSectionId()).isEqualTo(7);
        assertThat(result.getIndicatorId()).isEqualTo(20);
    }

    // ─── updateSectionIndicator ───────────────────────────────────────────────────

    @Test
    void updateSectionIndicator_updatesBothFieldsWhenBothProvided() {
        when(repository.findById(1L)).thenReturn(Optional.of(testSI));
        when(repository.save(testSI)).thenReturn(testSI);

        sectionIndicatorService.updateSectionIndicator(1L, 99, 88);

        assertThat(testSI.getSectionId()).isEqualTo(99);
        assertThat(testSI.getIndicatorId()).isEqualTo(88);
        verify(repository).save(testSI);
    }

    @Test
    void updateSectionIndicator_leavesSectionIdUnchangedWhenNull() {
        when(repository.findById(1L)).thenReturn(Optional.of(testSI));
        when(repository.save(testSI)).thenReturn(testSI);

        sectionIndicatorService.updateSectionIndicator(1L, null, 88);

        assertThat(testSI.getSectionId()).isEqualTo(SECTION_ID);
        assertThat(testSI.getIndicatorId()).isEqualTo(88);
    }

    @Test
    void updateSectionIndicator_leavesIndicatorIdUnchangedWhenNull() {
        when(repository.findById(1L)).thenReturn(Optional.of(testSI));
        when(repository.save(testSI)).thenReturn(testSI);

        sectionIndicatorService.updateSectionIndicator(1L, 99, null);

        assertThat(testSI.getSectionId()).isEqualTo(99);
        assertThat(testSI.getIndicatorId()).isEqualTo(INDICATOR_ID);
    }

    @Test
    void updateSectionIndicator_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionIndicatorService.updateSectionIndicator(99L, 1, 2))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).save(any());
    }

    // ─── removeSectionIndicator ───────────────────────────────────────────────────

    @Test
    void removeSectionIndicator_deletesWhenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(testSI));
        doNothing().when(repository).delete(testSI);

        sectionIndicatorService.removeSectionIndicator(1L);

        verify(repository).findById(1L);
        verify(repository).delete(testSI);
    }

    @Test
    void removeSectionIndicator_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionIndicatorService.removeSectionIndicator(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).delete(any());
    }

    // ─── searchSectionIndicators ──────────────────────────────────────────────────

    @Test
    void searchSectionIndicators_returnsMatchingResults() {
        SectionIndicatorRequest request = new SectionIndicatorRequest(null, List.of(SECTION_ID), null);
        when(repository.searchSectionIndicators(null, List.of(SECTION_ID), null))
                .thenReturn(List.of(testSI));

        List<SectionIndicator> results = sectionIndicatorService.searchSectionIndicators(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSectionId()).isEqualTo(SECTION_ID);
        verify(repository).searchSectionIndicators(null, List.of(SECTION_ID), null);
    }

    @Test
    void searchSectionIndicators_returnsAllWhenNoFiltersProvided() {
        SectionIndicator si2 = new SectionIndicator(6, 11, false);
        si2.setId(2L);
        SectionIndicatorRequest request = new SectionIndicatorRequest(null, null, null);
        when(repository.searchSectionIndicators(null, null, null))
                .thenReturn(List.of(testSI, si2));

        List<SectionIndicator> results = sectionIndicatorService.searchSectionIndicators(request);

        assertThat(results).hasSize(2);
    }

    @Test
    void searchSectionIndicators_returnsEmptyListWhenNoMatches() {
        SectionIndicatorRequest request = new SectionIndicatorRequest(List.of(999), null, null);
        when(repository.searchSectionIndicators(List.of(999), null, null)).thenReturn(List.of());

        List<SectionIndicator> results = sectionIndicatorService.searchSectionIndicators(request);

        assertThat(results).isEmpty();
    }

    @Test
    void searchSectionIndicators_canFilterByIndicatorId() {
        SectionIndicatorRequest request = new SectionIndicatorRequest(null, null, List.of(INDICATOR_ID));
        when(repository.searchSectionIndicators(null, null, List.of(INDICATOR_ID)))
                .thenReturn(List.of(testSI));

        List<SectionIndicator> results = sectionIndicatorService.searchSectionIndicators(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getIndicatorId()).isEqualTo(INDICATOR_ID);
    }
}
