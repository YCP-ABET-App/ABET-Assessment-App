package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.BaseServiceTest;
import com.abetappteam.abetapp.entity.Requests.ScheduleEntry.ScheduleEntrySearchRequest;
import com.abetappteam.abetapp.entity.ScheduleEntry;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.repository.ScheduleEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScheduleEntryServiceTest extends BaseServiceTest {

    @Mock
    private ScheduleEntryRepository repository;

    @InjectMocks
    private ScheduleEntryService scheduleEntryService;

    private ScheduleEntry testScheduleEntry;

    @BeforeEach
    void setUp() {
        testScheduleEntry = new ScheduleEntry(1, 10, 2, 3);
        testScheduleEntry.setId(1L);
    }

    @Test
    void shouldSearchScheduleEntries() {
        ScheduleEntrySearchRequest request = new ScheduleEntrySearchRequest(null, 1, 10, 2, 3);
        when(repository.searchScheduleEntry(request)).thenReturn(List.of(testScheduleEntry));

        List<ScheduleEntry> results = scheduleEntryService.searchScheduleEntry(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(1L);
        assertThat(results.get(0).getSemesterId()).isEqualTo(1);
        assertThat(results.get(0).getCourseId()).isEqualTo(10);
        assertThat(results.get(0).getProgramId()).isEqualTo(2);
        assertThat(results.get(0).getIndicatorId()).isEqualTo(3);
        verify(repository).searchScheduleEntry(request);
    }

    @Test
    void shouldCreateScheduleEntry() {
        when(repository.existsBySemesterIdAndCourseIdAndProgramIdAndIndicatorId(1, 10, 2, 3))
                .thenReturn(false);
        when(repository.save(any(ScheduleEntry.class))).thenReturn(testScheduleEntry);

        ScheduleEntry created = scheduleEntryService.createScheduleEntry(1, 10, 2, 3);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(1L);

        ArgumentCaptor<ScheduleEntry> captor = ArgumentCaptor.forClass(ScheduleEntry.class);
        verify(repository).existsBySemesterIdAndCourseIdAndProgramIdAndIndicatorId(1, 10, 2, 3);
        verify(repository).save(captor.capture());
        ScheduleEntry saved = captor.getValue();
        assertThat(saved.getSemesterId()).isEqualTo(1);
        assertThat(saved.getCourseId()).isEqualTo(10);
        assertThat(saved.getProgramId()).isEqualTo(2);
        assertThat(saved.getIndicatorId()).isEqualTo(3);
    }

    @Test
    void shouldThrowWhenDuplicateScheduleEntryExists() {
        when(repository.existsBySemesterIdAndCourseIdAndProgramIdAndIndicatorId(1, 10, 2, 3))
                .thenReturn(true);

        assertThatThrownBy(() -> scheduleEntryService.createScheduleEntry(1, 10, 2, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("This user is already assigned to the given section.");

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRemoveScheduleEntry() {
        when(repository.findById(1L)).thenReturn(Optional.of(testScheduleEntry));
        doNothing().when(repository).delete(testScheduleEntry);

        scheduleEntryService.removeScheduleEntry(1L);

        verify(repository).findById(1L);
        verify(repository).delete(testScheduleEntry);
    }

    @Test
    void shouldThrowWhenRemovingNonExistentScheduleEntry() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleEntryService.removeScheduleEntry(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ScheduleEntry not found with id: 999");

        verify(repository, never()).delete(any());
    }
}
