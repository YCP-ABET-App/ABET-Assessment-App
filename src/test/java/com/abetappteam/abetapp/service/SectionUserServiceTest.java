package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.BaseServiceTest;
import com.abetappteam.abetapp.entity.Requests.SectionUser.SectionUserSearchRequest;
import com.abetappteam.abetapp.entity.SectionUser;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.repository.SectionUserRepository;
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

class SectionUserServiceTest extends BaseServiceTest {

    @Mock
    private SectionUserRepository repository;

    @InjectMocks
    private SectionUserService sectionUserService;

    private SectionUser testSectionUser;

    @BeforeEach
    void setUp() {
        testSectionUser = new SectionUser();
        testSectionUser.setId(1L);
        testSectionUser.setSectionId(1);
        testSectionUser.setUserId(2);
    }

    @Test
    void shouldSearchSectionUsers() {
        SectionUserSearchRequest request = new SectionUserSearchRequest(null, 1, 2);
        when(repository.searchSectionUser(request)).thenReturn(List.of(testSectionUser));

        List<SectionUser> results = sectionUserService.searchSectionUser(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(1L);
        verify(repository).searchSectionUser(request);
    }

    @Test
    void shouldCreateSectionUser() {
        when(repository.existsBySectionIdAndUserId(1, 2)).thenReturn(false);
        when(repository.save(any(SectionUser.class))).thenReturn(testSectionUser);

        SectionUser created = sectionUserService.createSectionUser(1, 2);

        assertThat(created).isNotNull();
        assertThat(created.getSectionId()).isEqualTo(1);
        assertThat(created.getUserId()).isEqualTo(2);
        verify(repository).existsBySectionIdAndUserId(1, 2);
        verify(repository).save(any(SectionUser.class));
    }

    @Test
    void shouldThrowWhenDuplicateSectionUserExists() {
        when(repository.existsBySectionIdAndUserId(1, 2)).thenReturn(true);

        assertThatThrownBy(() -> sectionUserService.createSectionUser(1, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("This user is already assigned to the given section.");

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRemoveSectionUser() {
        when(repository.findById(1L)).thenReturn(Optional.of(testSectionUser));
        doNothing().when(repository).delete(testSectionUser);

        sectionUserService.removeSectionUser(1L);

        verify(repository).findById(1L);
        verify(repository).delete(testSectionUser);
    }

    @Test
    void shouldThrowWhenRemovingNonExistentSectionUser() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionUserService.removeSectionUser(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("SectionUser not found with id: 999");

        verify(repository, never()).delete(any());
    }
}