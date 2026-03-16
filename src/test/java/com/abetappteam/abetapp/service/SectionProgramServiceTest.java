package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.BaseServiceTest;
import com.abetappteam.abetapp.entity.Requests.SectionProgram.SectionProgramSearchRequest;
import com.abetappteam.abetapp.entity.SectionProgram;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.repository.SectionProgramRepository;
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

class SectionProgramServiceTest extends BaseServiceTest {

    @Mock
    private SectionProgramRepository repository;

    @InjectMocks
    private SectionProgramService sectionProgramService;

    private SectionProgram testSectionProgram;

    @BeforeEach
    void setUp() {
        testSectionProgram = new SectionProgram();
        testSectionProgram.setId(1L);
        testSectionProgram.setSectionId(1);
        testSectionProgram.setProgramId(2);
    }

    @Test
    void shouldSearchSectionPrograms() {
        SectionProgramSearchRequest request = new SectionProgramSearchRequest(null, 1, 2);
        when(repository.searchSectionProgram(request)).thenReturn(List.of(testSectionProgram));

        List<SectionProgram> results = sectionProgramService.searchSectionProgram(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(1L);
        verify(repository).searchSectionProgram(request);
    }

    @Test
    void shouldCreateSectionProgram() {
        when(repository.existsBySectionIdAndProgramId(1, 2)).thenReturn(false);
        when(repository.save(any(SectionProgram.class))).thenReturn(testSectionProgram);

        SectionProgram created = sectionProgramService.createSectionProgram(1, 2);

        assertThat(created).isNotNull();
        assertThat(created.getSectionId()).isEqualTo(1);
        assertThat(created.getProgramId()).isEqualTo(2);
        verify(repository).existsBySectionIdAndProgramId(1, 2);
        verify(repository).save(any(SectionProgram.class));
    }

    @Test
    void shouldThrowWhenDuplicateSectionProgramExists() {
        when(repository.existsBySectionIdAndProgramId(1, 2)).thenReturn(true);

        assertThatThrownBy(() -> sectionProgramService.createSectionProgram(1, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("This section is already assigned to the given program.");

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRemoveSectionProgram() {
        when(repository.findById(1L)).thenReturn(Optional.of(testSectionProgram));
        doNothing().when(repository).delete(testSectionProgram);

        sectionProgramService.removeSectionProgram(1L);

        verify(repository).findById(1L);
        verify(repository).delete(testSectionProgram);
    }

    @Test
    void shouldThrowWhenRemovingNonExistentSectionProgram() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionProgramService.removeSectionProgram(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("SectionProgram not found with id: 999");

        verify(repository, never()).delete(any());
    }
}