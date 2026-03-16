package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.BaseServiceTest;
import com.abetappteam.abetapp.dto.SemesterDTO;
import com.abetappteam.abetapp.entity.Semester;
import com.abetappteam.abetapp.entity.Semester.SemesterStatus;
import com.abetappteam.abetapp.entity.Semester.SemesterType;
import com.abetappteam.abetapp.exception.BusinessException;
import com.abetappteam.abetapp.exception.ConflictException;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.repository.SemesterRepository;
import com.abetappteam.abetapp.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SemesterServiceTest extends BaseServiceTest {

    @Mock
    private SemesterRepository semesterRepository;

    @InjectMocks
    private SemesterService semesterService;

    private Semester testSemester;
    private SemesterDTO testSemesterDTO;

    @BeforeEach
    void setUp() {
        testSemester = TestDataBuilder.createSemesterWithId(1L, "Fall 2024", "FALL-2024",
                LocalDate.of(2024, 9, 1), LocalDate.of(2024, 12, 15),
                2024, SemesterType.FALL, 1L, "Fall Semester 2024", false);
        testSemester.setStatus(SemesterStatus.UPCOMING);

        testSemesterDTO = TestDataBuilder.createSemesterDTO("Spring 2025", "SPRING-2025",
                LocalDate.of(2025, 1, 15), LocalDate.of(2025, 5, 15),
                2025, "SPRING", 1L, "Spring Semester 2025", false);
    }

    @Test
    void shouldFindById() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(testSemester));

        Semester found = semesterService.findById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
        assertThat(found.getName()).isEqualTo("Fall 2024");
        verify(semesterRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenNotFound() {
        when(semesterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> semesterService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Semester not found with id: 999");
    }

    @Test
    void shouldSearchSemesters() {
        when(semesterRepository.searchSemesters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(testSemester));

        var request = new com.abetappteam.abetapp.entity.Requests.Semester.SemesterSearchRequest(
                null, null, null, null, null, null, null, null);
        List<Semester> results = semesterService.searchSemesters(request);

        assertThat(results).hasSize(1);
        verify(semesterRepository).searchSemesters(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldCreateSemester() {
        // No duplicate found
        when(semesterRepository.searchSemesters(any(), any(), any(), any(), any(), any(), any(), eq("SPRING-2025")))
                .thenReturn(List.of());
        when(semesterRepository.save(any(Semester.class))).thenReturn(testSemester);

        Semester created = semesterService.createSemester(testSemesterDTO);

        assertThat(created).isNotNull();
        verify(semesterRepository).save(any(Semester.class));
    }

    @Test
    void shouldThrowConflictWhenCreatingDuplicateCode() {
        when(semesterRepository.searchSemesters(any(), any(), any(), any(), any(), any(), any(), eq("SPRING-2025")))
                .thenReturn(List.of(testSemester));

        assertThatThrownBy(() -> semesterService.createSemester(testSemesterDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Semester with code 'SPRING-2025' already exists");

        verify(semesterRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessExceptionWhenEndDateBeforeStartDate() {
        SemesterDTO invalidDTO = TestDataBuilder.createSemesterDTO("Invalid", "INVALID-2025",
                LocalDate.of(2025, 5, 15), LocalDate.of(2025, 1, 15),
                2025, "SPRING", 1L, "Invalid dates", false);

        when(semesterRepository.searchSemesters(any(), any(), any(), any(), any(), any(), any(), eq("INVALID-2025")))
                .thenReturn(List.of());

        assertThatThrownBy(() -> semesterService.createSemester(invalidDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("End date cannot be before start date");

        verify(semesterRepository, never()).save(any());
    }

    @Test
    void shouldUpdateSemester() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(testSemester));
        when(semesterRepository.searchSemesters(any(), any(), any(), any(), any(), any(), any(), eq("SPRING-2025")))
                .thenReturn(List.of());
        when(semesterRepository.save(any(Semester.class))).thenReturn(testSemester);

        Semester updated = semesterService.updateSemester(1L, testSemesterDTO);

        assertThat(updated).isNotNull();
        verify(semesterRepository).findById(1L);
        verify(semesterRepository).save(any(Semester.class));
    }

    @Test
    void shouldThrowConflictWhenUpdatingWithDuplicateCode() {
        Semester otherSemester = TestDataBuilder.createSemesterWithId(2L, "Spring 2025", "SPRING-2025",
                LocalDate.of(2025, 1, 15), LocalDate.of(2025, 5, 15),
                2025, SemesterType.SPRING, 1L, "Another semester", false);

        when(semesterRepository.findById(1L)).thenReturn(Optional.of(testSemester));
        when(semesterRepository.searchSemesters(any(), any(), any(), any(), any(), any(), any(), eq("SPRING-2025")))
                .thenReturn(List.of(otherSemester));

        assertThatThrownBy(() -> semesterService.updateSemester(1L, testSemesterDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Semester with code 'SPRING-2025' already exists");

        verify(semesterRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessExceptionWhenUpdatingNonEditableSemester() {
        testSemester.setStatus(SemesterStatus.COMPLETED);
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(testSemester));

        assertThatThrownBy(() -> semesterService.updateSemester(1L, testSemesterDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot edit semester that is completed or archived");

        verify(semesterRepository, never()).save(any());
    }

    @Test
    void shouldRemoveSemester() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(testSemester));
        doNothing().when(semesterRepository).delete(testSemester);

        semesterService.removeSemester(1L);

        verify(semesterRepository).findById(1L);
        verify(semesterRepository).delete(testSemester);
    }

    @Test
    void shouldUpdateSemesterStatus() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(testSemester));
        when(semesterRepository.save(any(Semester.class))).thenReturn(testSemester);

        Semester updated = semesterService.updateSemesterStatus(1L, SemesterStatus.ACTIVE);

        assertThat(updated.getStatus()).isEqualTo(SemesterStatus.ACTIVE);
        verify(semesterRepository).save(testSemester);
    }

    @Test
    void shouldThrowWhenInvalidStatusTransitionFromArchived() {
        testSemester.setStatus(SemesterStatus.ARCHIVED);
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(testSemester));

        assertThatThrownBy(() -> semesterService.updateSemesterStatus(1L, SemesterStatus.ACTIVE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot change status of archived semester");

        verify(semesterRepository, never()).save(any());
    }

    @Test
    void shouldSetAsCurrentSemester() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(testSemester));
        doNothing().when(semesterRepository).clearCurrentSemesterFlag(1L);
        when(semesterRepository.save(any(Semester.class))).thenReturn(testSemester);

        Semester updated = semesterService.setAsCurrentSemester(1L);

        assertThat(updated.getIsCurrent()).isTrue();
        verify(semesterRepository).clearCurrentSemesterFlag(1L);
        verify(semesterRepository).save(testSemester);
    }

    @Test
    void shouldClearCurrentSemesterFlag() {
        doNothing().when(semesterRepository).clearCurrentSemesterFlag(1L);

        semesterService.clearCurrentSemesterFlag(1L);

        verify(semesterRepository).clearCurrentSemesterFlag(1L);
    }

    @Test
    void shouldUpdateAllSemesterStatuses() {
        List<Semester> semesters = TestDataBuilder.createSemesterList(2, 1L);
        semesters.forEach(s -> s.setStatus(SemesterStatus.UPCOMING));
        when(semesterRepository.findAll()).thenReturn(semesters);
        when(semesterRepository.save(any(Semester.class))).thenAnswer(i -> i.getArgument(0));

        semesterService.updateAllSemesterStatuses();

        verify(semesterRepository).findAll();
        verify(semesterRepository, times(semesters.size())).save(any(Semester.class));
    }
}