package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.BaseServiceTest;
import com.abetappteam.abetapp.entity.Requests.ScheduleEntry.ScheduleEntrySearchRequest;
import com.abetappteam.abetapp.entity.ScheduleEntry;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.repository.ScheduleEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScheduleEntryServiceTest extends BaseServiceTest {

    @Mock
    private ScheduleEntryRepository repository;

    @InjectMocks
    private ScheduleEntryService scheduleEntryService;

    private ScheduleEntry testEntry;

    // Fixed values: semesterId=1, courseId=2, programId=3, indicatorId=4
    private static final int SEMESTER_ID = 1;
    private static final int COURSE_ID = 2;
    private static final int PROGRAM_ID = 3;
    private static final int INDICATOR_ID = 4;

    @BeforeEach
    void setUp() {
        testEntry = new ScheduleEntry(SEMESTER_ID, COURSE_ID, PROGRAM_ID, INDICATOR_ID);
        testEntry.setId(10L);
    }

    // ─── createScheduleEntry ─────────────────────────────────────────────────────

    @Test
    void createScheduleEntry_savesAndReturnsNewEntry() {
        when(repository.existsBySemesterIdAndCourseIdAndProgramIdAndIndicatorId(
                SEMESTER_ID, COURSE_ID, PROGRAM_ID, INDICATOR_ID)).thenReturn(false);
        when(repository.save(any(ScheduleEntry.class))).thenReturn(testEntry);

        ScheduleEntry result = scheduleEntryService.createScheduleEntry(
                SEMESTER_ID, COURSE_ID, PROGRAM_ID, INDICATOR_ID);

        assertThat(result).isNotNull();
        assertThat(result.getSemesterId()).isEqualTo(SEMESTER_ID);
        assertThat(result.getCourseId()).isEqualTo(COURSE_ID);
        assertThat(result.getProgramId()).isEqualTo(PROGRAM_ID);
        assertThat(result.getIndicatorId()).isEqualTo(INDICATOR_ID);
        verify(repository).save(any(ScheduleEntry.class));
    }

    @Test
    void createScheduleEntry_throwsWhenDuplicateMappingExists() {
        when(repository.existsBySemesterIdAndCourseIdAndProgramIdAndIndicatorId(
                SEMESTER_ID, COURSE_ID, PROGRAM_ID, INDICATOR_ID)).thenReturn(true);

        assertThatThrownBy(() ->
                scheduleEntryService.createScheduleEntry(SEMESTER_ID, COURSE_ID, PROGRAM_ID, INDICATOR_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already assigned");

        verify(repository, never()).save(any());
    }

    @Test
    void createScheduleEntry_checksExistenceBeforeSaving() {
        when(repository.existsBySemesterIdAndCourseIdAndProgramIdAndIndicatorId(
                SEMESTER_ID, COURSE_ID, PROGRAM_ID, INDICATOR_ID)).thenReturn(false);
        when(repository.save(any(ScheduleEntry.class))).thenReturn(testEntry);

        scheduleEntryService.createScheduleEntry(SEMESTER_ID, COURSE_ID, PROGRAM_ID, INDICATOR_ID);

        verify(repository).existsBySemesterIdAndCourseIdAndProgramIdAndIndicatorId(
                SEMESTER_ID, COURSE_ID, PROGRAM_ID, INDICATOR_ID);
        verify(repository).save(any(ScheduleEntry.class));
    }

    // ─── removeScheduleEntry ─────────────────────────────────────────────────────

    @Test
    void removeScheduleEntry_deletesEntryWhenFound() {
        when(repository.findById(10L)).thenReturn(Optional.of(testEntry));
        doNothing().when(repository).delete(testEntry);

        scheduleEntryService.removeScheduleEntry(10L);

        verify(repository).findById(10L);
        verify(repository).delete(testEntry);
    }

    @Test
    void removeScheduleEntry_throwsWhenEntryNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleEntryService.removeScheduleEntry(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).delete(any());
    }

    // ─── searchScheduleEntry ──────────────────────────────────────────────────────

    @Test
    void searchScheduleEntry_returnsMatchingEntries() {
        ScheduleEntrySearchRequest request = new ScheduleEntrySearchRequest(
                null, SEMESTER_ID, COURSE_ID, PROGRAM_ID, INDICATOR_ID);
        when(repository.searchScheduleEntry(request)).thenReturn(List.of(testEntry));

        List<ScheduleEntry> results = scheduleEntryService.searchScheduleEntry(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(10L);
        verify(repository).searchScheduleEntry(request);
    }

    @Test
    void searchScheduleEntry_returnsEmptyListWhenNoEntriesMatch() {
        ScheduleEntrySearchRequest request = new ScheduleEntrySearchRequest(null, 99, null, null, null);
        when(repository.searchScheduleEntry(request)).thenReturn(List.of());

        List<ScheduleEntry> results = scheduleEntryService.searchScheduleEntry(request);

        assertThat(results).isEmpty();
    }

    @Test
    void searchScheduleEntry_returnsAllEntriesWhenNoFiltersProvided() {
        ScheduleEntry entry2 = new ScheduleEntry(5, 6, 7, 8);
        entry2.setId(20L);
        ScheduleEntrySearchRequest request = new ScheduleEntrySearchRequest(null, null, null, null, null);
        when(repository.searchScheduleEntry(request)).thenReturn(List.of(testEntry, entry2));

        List<ScheduleEntry> results = scheduleEntryService.searchScheduleEntry(request);

        assertThat(results).hasSize(2);
    }

    @Test
    void searchScheduleEntry_canFilterById() {
        ScheduleEntrySearchRequest request = new ScheduleEntrySearchRequest(10, null, null, null, null);
        when(repository.searchScheduleEntry(request)).thenReturn(List.of(testEntry));

        List<ScheduleEntry> results = scheduleEntryService.searchScheduleEntry(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(10L);
    }
}
