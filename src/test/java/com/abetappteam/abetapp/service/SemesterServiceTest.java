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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

        testSemesterDTO = TestDataBuilder.createSemesterDTO("Spring 2025", "SPRING-2025",
                LocalDate.of(2025, 1, 15), LocalDate.of(2025, 5, 15),
                2025, "SPRING", 1L, "Spring Semester 2025", true);
    }

    @Test
    void shouldFindById() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(testSemester));

        Semester found = semesterService.findById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
        verify(semesterRepository).findById(1L);
    }

    @Test
    void shouldCreateSemester() {
        when(semesterRepository.findByCodeIgnoreCaseAndProgramId("SPRING-2025", 1L))
                .thenReturn(Optional.empty());
        when(semesterRepository.save(any(Semester.class))).thenReturn(testSemester);

        Semester created = semesterService.createSemester(testSemesterDTO);

        assertThat(created).isNotNull();
        verify(semesterRepository).save(any(Semester.class));
    }

    @Test
    void shouldUpdateSemester() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(testSemester));
        when(semesterRepository.findByCodeIgnoreCaseAndProgramId("SPRING-2025", 1L))
                .thenReturn(Optional.empty());
        when(semesterRepository.save(any(Semester.class))).thenReturn(testSemester);

        Semester updated = semesterService.updateSemester(1L, testSemesterDTO);

        assertThat(updated).isNotNull();
        verify(semesterRepository).save(any(Semester.class));
    }

    @Test
    void shouldRemoveSemester() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(testSemester));
        when(semesterRepository.hasCourses(1L)).thenReturn(false);

        semesterService.removeSemester(1L);

        verify(semesterRepository).delete(testSemester);
    }

    @Test
    void shouldGetSemestersByProgram() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Semester> semesters = TestDataBuilder.createSemesterList(3, 1L);
        Page<Semester> page = new PageImpl<>(semesters, pageable, 3);
        when(semesterRepository.findByProgramId(1L, pageable)).thenReturn(page);

        Page<Semester> found = semesterService.getSemestersByProgram(1L, pageable);

        assertThat(found.getContent()).hasSize(3);
    }

    @Test
    void shouldGetSemestersByProgramList() {
        List<Semester> semesters = TestDataBuilder.createSemesterList(3, 1L);
        when(semesterRepository.findByProgramId(1L)).thenReturn(semesters);

        List<Semester> found = semesterService.getSemestersByProgram(1L);

        assertThat(found).hasSize(3);
    }

    @Test
    void shouldGetSemestersByAcademicYear() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Semester> semesters = TestDataBuilder.createSemesterList(2, 1L);
        Page<Semester> page = new PageImpl<>(semesters, pageable, 2);
        when(semesterRepository.findByAcademicYear(2024, pageable)).thenReturn(page);

        Page<Semester> found = semesterService.getSemestersByAcademicYear(2024, pageable);

        assertThat(found.getContent()).hasSize(2);
    }

    @Test
    void shouldGetSemestersByType() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Semester> semesters = TestDataBuilder.createSemesterList(2, 1L);
        Page<Semester> page = new PageImpl<>(semesters, pageable, 2);
        when(semesterRepository.findByType(SemesterType.FALL, pageable)).thenReturn(page);

        Page<Semester> found = semesterService.getSemestersByType(SemesterType.FALL, pageable);

        assertThat(found.getContent()).hasSize(2);
    }

    @Test
    void shouldGetSemestersByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Semester> semesters = TestDataBuilder.createSemesterList(2, 1L);
        Page<Semester> page = new PageImpl<>(semesters, pageable, 2);
        when(semesterRepository.findByStatus(SemesterStatus.ACTIVE, pageable)).thenReturn(page);

        Page<Semester> found = semesterService.getSemestersByStatus(SemesterStatus.ACTIVE, pageable);

        assertThat(found.getContent()).hasSize(2);
    }

    @Test
    void shouldGetSemestersByProgramAndAcademicYear() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Semester> semesters = TestDataBuilder.createSemesterList(2, 1L);
        Page<Semester> page = new PageImpl<>(semesters, pageable, 2);
        when(semesterRepository.findByProgramIdAndAcademicYear(1L, 2024, pageable)).thenReturn(page);

        Page<Semester> found = semesterService.getSemestersByProgramAndAcademicYear(1L, 2024, pageable);

        assertThat(found.getContent()).hasSize(2);
    }

    @Test
    void shouldGetSemestersByProgramAndType() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Semester> semesters = TestDataBuilder.createSemesterList(2, 1L);
        Page<Semester> page = new PageImpl<>(semesters, pageable, 2);
        when(semesterRepository.findByProgramIdAndType(1L, SemesterType.FALL, pageable)).thenReturn(page);

        Page<Semester> found = semesterService.getSemestersByProgramAndType(1L, SemesterType.FALL, pageable);

        assertThat(found.getContent()).hasSize(2);
    }

    @Test
    void shouldGetSemestersByProgramAndStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Semester> semesters = TestDataBuilder.createSemesterList(2, 1L);
        Page<Semester> page = new PageImpl<>(semesters, pageable, 2);
        when(semesterRepository.findByProgramIdAndStatus(1L, SemesterStatus.ACTIVE, pageable))
                .thenReturn(page);

        Page<Semester> found = semesterService.getSemestersByProgramAndStatus(1L, SemesterStatus.ACTIVE, pageable);

        assertThat(found.getContent()).hasSize(2);
    }

    @Test
    void shouldFindByCode() {
        when(semesterRepository.findByCodeIgnoreCase("FALL-2024")).thenReturn(Optional.of(testSemester));

        Semester found = semesterService.findByCode("FALL-2024");

        assertThat(found.getCode()).isEqualTo("FALL-2024");
    }

    @Test
    void shouldCheckExistsByCode() {
        when(semesterRepository.existsByCodeIgnoreCase("FALL-2024")).thenReturn(true);

        boolean exists = semesterService.existsByCode("FALL-2024");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldSearchByNameOrCode() {
        List<Semester> semesters = TestDataBuilder.createSemesterList(2, 1L);
        when(semesterRepository.searchByNameOrCode("Fall")).thenReturn(semesters);

        List<Semester> found = semesterService.searchByNameOrCode("Fall");

        assertThat(found).hasSize(2);
    }

    @Test
    void shouldSearchByNameOrCodePaginated() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Semester> semesters = TestDataBuilder.createSemesterList(2, 1L);
        Page<Semester> page = new PageImpl<>(semesters, pageable, 2);
        when(semesterRepository.searchByNameOrCode("Fall", pageable)).thenReturn(page);

        Page<Semester> found = semesterService.searchByNameOrCode("Fall", pageable);

        assertThat(found.getContent()).hasSize(2);
    }

    @Test
    void shouldGetActiveSemestersOnDate() {
        LocalDate date = LocalDate.of(2024, 10, 1);
        List<Semester> semesters = TestDataBuilder.createSemesterList(2, 1L);
        when(semesterRepository.findActiveSemestersOnDate(date)).thenReturn(semesters);

        List<Semester> found = semesterService.getActiveSemestersOnDate(date);

        assertThat(found).hasSize(2);
    }

    @Test
    void shouldGetCurrentSemesterByProgram() {
        when(semesterRepository.findCurrentSemesterByProgram(1L)).thenReturn(Optional.of(testSemester));

        Optional<Semester> found = semesterService.getCurrentSemesterByProgram(1L);

        assertThat(found).isPresent();
    }

    @Test
    void shouldGetCurrentSemesters() {
        List<Semester> semesters = TestDataBuilder.createSemesterList(2, 1L);
        when(semesterRepository.findByIsCurrentTrue()).thenReturn(semesters);

        List<Semester> found = semesterService.getCurrentSemesters();

        assertThat(found).hasSize(2);
    }

    @Test
    void shouldGetActiveAndUpcomingSemestersByProgram() {
        List<Semester> semesters = TestDataBuilder.createSemesterList(3, 1L);
        when(semesterRepository.findActiveAndUpcomingSemestersByProgram(1L)).thenReturn(semesters);

        List<Semester> found = semesterService.getActiveAndUpcomingSemestersByProgram(1L);

        assertThat(found).hasSize(3);
    }

    @Test
    void shouldGetDistinctAcademicYearsByProgram() {
        List<Semester> semesters = TestDataBuilder.createSemesterList(3, 1L);
        when(semesterRepository.findByProgramId(1L)).thenReturn(semesters);

        List<Integer> years = semesterService.getDistinctAcademicYearsByProgram(1L);

        assertThat(years).isNotEmpty();
    }

    @Test
    void shouldCountByProgram() {
        when(semesterRepository.countByProgramId(1L)).thenReturn(5L);

        long count = semesterService.countByProgram(1L);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    void shouldCountByProgramAndStatus() {
        when(semesterRepository.countByProgramIdAndStatus(1L, SemesterStatus.ACTIVE)).thenReturn(2L);

        long count = semesterService.countByProgramAndStatus(1L, SemesterStatus.ACTIVE);

        assertThat(count).isEqualTo(2L);
    }

    @Test
    void shouldUpdateSemesterStatus() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(testSemester));
        when(semesterRepository.save(any(Semester.class))).thenReturn(testSemester);

        Semester updated = semesterService.updateSemesterStatus(1L, SemesterStatus.ACTIVE);

        assertThat(updated.getStatus()).isEqualTo(SemesterStatus.ACTIVE);
    }

    @Test
    void shouldSetAsCurrentSemester() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(testSemester));
        when(semesterRepository.save(any(Semester.class))).thenReturn(testSemester);

        Semester updated = semesterService.setAsCurrentSemester(1L);

        assertThat(updated.getIsCurrent()).isTrue();
        verify(semesterRepository).clearCurrentSemesterFlag(1L);
    }

    @Test
    void shouldClearCurrentSemesterFlag() {
        semesterService.clearCurrentSemesterFlag(1L);

        verify(semesterRepository).clearCurrentSemesterFlag(1L);
    }

    @Test
    void shouldCheckHasCourses() {
        when(semesterRepository.hasCourses(1L)).thenReturn(true);

        boolean hasCourses = semesterService.hasCourses(1L);

        assertThat(hasCourses).isTrue();
    }

    @Test
    void shouldCountCoursesBySemester() {
        when(semesterRepository.countCoursesBySemesterId(1L)).thenReturn(10L);

        long count = semesterService.countCoursesBySemester(1L);

        assertThat(count).isEqualTo(10L);
    }

    @Test
    void shouldUpdateAllSemesterStatuses() {
        List<Semester> semesters = TestDataBuilder.createSemesterList(3, 1L);
        when(semesterRepository.findAll()).thenReturn(semesters);
        when(semesterRepository.save(any(Semester.class))).thenReturn(testSemester);

        semesterService.updateAllSemesterStatuses();

        verify(semesterRepository, atLeastOnce()).save(any(Semester.class));
    }
}