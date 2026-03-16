package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.BaseServiceTest;
import com.abetappteam.abetapp.entity.Requests.Section.SectionSearchRequest;
import com.abetappteam.abetapp.entity.Section;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.repository.SectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SectionServiceTest extends BaseServiceTest {

    @Mock
    private SectionRepository repository;

    @InjectMocks
    private SectionService sectionService;

    private Section testSection;

    @BeforeEach
    void setUp() {
        testSection = new Section("001", 1, 1);
        testSection.setId(1L);
    }

    @Test
    void shouldSearchSections() {
        SectionSearchRequest request = new SectionSearchRequest(null, null, null, 1, null);
        when(repository.searchSections(request)).thenReturn(List.of(testSection));

        List<Section> results = sectionService.searchSections(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(1L);
        verify(repository).searchSections(request);
    }

    @Test
    void shouldCreateSection() {
        when(repository.existsByCourseNumberAndSemesterIdAndCourseId("001", 1, 1)).thenReturn(false);
        when(repository.save(any(Section.class))).thenReturn(testSection);

        Section created = sectionService.createSection("001", 1, 1);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(1L);
        verify(repository).existsByCourseNumberAndSemesterIdAndCourseId("001", 1, 1);
        verify(repository).save(any(Section.class));
    }

    @Test
    void shouldThrowWhenDuplicateSectionExists() {
        when(repository.existsByCourseNumberAndSemesterIdAndCourseId("001", 1, 1)).thenReturn(true);

        assertThatThrownBy(() -> sectionService.createSection("001", 1, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A section with the same section number already exists");

        verify(repository, never()).save(any());
    }

    @Test
    void shouldUpdateSection() {
        when(repository.findById(1L)).thenReturn(Optional.of(testSection));
        when(repository.existsByCourseNumberAndSemesterIdAndCourseId("002", 1, 1)).thenReturn(false);
        when(repository.save(any(Section.class))).thenReturn(testSection);

        Section updated = sectionService.updateSection(1L, "002", 1, 1);

        assertThat(updated).isNotNull();
        verify(repository).findById(1L);
        verify(repository).existsByCourseNumberAndSemesterIdAndCourseId("002", 1, 1);
        verify(repository).save(any(Section.class));
    }

    @Test
    void shouldThrowWhenUpdatingWithDuplicateSectionNumber() {
        when(repository.findById(1L)).thenReturn(Optional.of(testSection));
        when(repository.existsByCourseNumberAndSemesterIdAndCourseId("002", 1, 1)).thenReturn(true);

        assertThatThrownBy(() -> sectionService.updateSection(1L, "002", 1, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A section with the same section number already exists");

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRemoveSection() {
        when(repository.findById(1L)).thenReturn(Optional.of(testSection));
        doNothing().when(repository).delete(testSection);

        sectionService.removeSection(1L);

        verify(repository).findById(1L);
        verify(repository).delete(testSection);
    }

    @Test
    void shouldThrowWhenRemovingNonExistentSection() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.removeSection(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Section not found with id: 999");

        verify(repository, never()).delete(any());
    }
}