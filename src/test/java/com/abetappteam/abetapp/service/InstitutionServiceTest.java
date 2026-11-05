package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.BaseServiceTest;
import com.abetappteam.abetapp.dto.InstitutionDTO;
import com.abetappteam.abetapp.entity.Institution;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.repository.InstitutionRepository;
import com.abetappteam.abetapp.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InstitutionServiceTest extends BaseServiceTest {

    @Mock
    private InstitutionRepository institutionRepository;

    @InjectMocks
    private InstitutionService institutionService;

    private Institution testInstitution;
    private InstitutionDTO testDTO;

    @BeforeEach
    void setUp() {
        testInstitution = TestDataBuilder.createInstitutionWithId(1L, "Example University", 13158, "ycp");
        testDTO = TestDataBuilder.createInstitutionDTO();
    }

    @Test
    void shouldFindById() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(testInstitution));

        Institution found = institutionService.findById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
        assertThat(found.getName()).isEqualTo("Example University");
        verify(institutionRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenNotFound() {
        when(institutionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> institutionService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Institution not found with id: 999");
    }

    @Test
    void shouldFindAllPaginated() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Institution> institutions = TestDataBuilder.createInstitutionList(3);
        Page<Institution> page = new PageImpl<>(institutions, pageable, 3);
        when(institutionRepository.findAll(pageable)).thenReturn(page);

        Page<Institution> found = institutionService.findAll(pageable);

        assertThat(found.getContent()).hasSize(3);
        verify(institutionRepository).findAll(pageable);
    }

    @Test
    void shouldCreateInstitution() {
        when(institutionRepository.existsByNameIgnoreCase(any())).thenReturn(false);
        when(institutionRepository.save(any(Institution.class))).thenReturn(testInstitution);

        Institution created = institutionService.create(testDTO);

        assertThat(created).isNotNull();
        verify(institutionRepository).save(any(Institution.class));
    }

    @Test
    void shouldUpdateInstitution() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(testInstitution));
        when(institutionRepository.findByNameIgnoreCase(any())).thenReturn(Optional.empty());
        when(institutionRepository.save(any(Institution.class))).thenReturn(testInstitution);

        Institution updated = institutionService.update(1L, testDTO);

        assertThat(updated).isNotNull();
        verify(institutionRepository).findById(1L);
        verify(institutionRepository).save(any(Institution.class));
    }

    @Test
    void shouldDeleteInstitution() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(testInstitution));
        doNothing().when(institutionRepository).delete(testInstitution);

        institutionService.delete(1L);

        verify(institutionRepository).findById(1L);
        verify(institutionRepository).delete(testInstitution);
    }
}
