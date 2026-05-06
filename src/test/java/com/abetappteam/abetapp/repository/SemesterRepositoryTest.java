package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.BaseRepositoryTest;
import com.abetappteam.abetapp.entity.Semester;
import com.abetappteam.abetapp.util.TestDataBuilder;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for SemesterRepository
 */
@Transactional
@Execution(ExecutionMode.SAME_THREAD)
class SemesterRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private SemesterRepository semesterRepository;

    private Semester testSemester;

    @BeforeEach
    void setUp() {
        semesterRepository.deleteAll();
        flush();
        clearContext();
        testSemester = TestDataBuilder.createSemester("Fall 2024", "FALL-2024",
                LocalDate.of(2024, 9, 1), LocalDate.of(2024, 12, 15),
                2024, Semester.SemesterType.FALL, 1L);
    }

    @AfterEach
    void tearDown() {
        semesterRepository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveSemester() {
        // Given
        Semester saved = semesterRepository.save(testSemester);
        flush();
        clearContext();

        // When
        Optional<Semester> found = semesterRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Fall 2024");
        assertThat(found.get().getCode()).isEqualTo("FALL-2024");
        assertThat(found.get().getProgramId()).isEqualTo(1L);
    }
    // TODO: Come through and refactor these tests with updated search code

    @Test
    void shouldFindByIsCurrentTrue() {
        // Given
        Semester currentSemester = TestDataBuilder.createCurrentSemester();
        semesterRepository.save(currentSemester);
        semesterRepository.save(testSemester); // Non-current semester
        flush();
        clearContext();

        // When
        List<Semester> currentSemesters = semesterRepository.findByIsCurrentTrue();

        // Then
        assertThat(currentSemesters).hasSize(1);
        assertThat(currentSemesters.get(0).getIsCurrent()).isTrue();
    }

    @Test
    void shouldFindCurrentSemesterByProgram() {
        // Given
        Semester currentSemester = TestDataBuilder.createCurrentSemester();
        semesterRepository.save(currentSemester);
        semesterRepository.save(testSemester);
        flush();
        clearContext();

        // When
        Optional<Semester> found = semesterRepository.findCurrentSemesterByProgram(currentSemester.getProgramId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getIsCurrent()).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenNoCurrentSemesterForProgram() {
        // Given
        semesterRepository.save(testSemester);
        flush();
        clearContext();

        // When
        Optional<Semester> found = semesterRepository.findCurrentSemesterByProgram(testSemester.getProgramId());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldSearchByName() {
        semesterRepository.save(testSemester);
        semesterRepository.save(TestDataBuilder.createSemester("Spring 2024", "SPRING-2024",
                LocalDate.of(2024, 1, 15), LocalDate.of(2024, 5, 15),
                2024, Semester.SemesterType.SPRING, 1L));
        flush();
        clearContext();

        List<Semester> found = semesterRepository.searchByNameOrCode("Fall");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Fall 2024");
    }

    @Test
    void shouldSearchByCode() {
        semesterRepository.save(testSemester);
        semesterRepository.save(TestDataBuilder.createSemester("Spring 2024", "SPRING-2024",
                LocalDate.of(2024, 1, 15), LocalDate.of(2024, 5, 15),
                2024, Semester.SemesterType.SPRING, 1L));
        flush();
        clearContext();

        List<Semester> found = semesterRepository.searchByNameOrCode("SPRING");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getCode()).isEqualTo("SPRING-2024");
    }

    @Test
    void shouldCountByProgramId() {
        semesterRepository.save(testSemester);
        semesterRepository.save(TestDataBuilder.createSemester("Spring 2024", "SPRING-2024",
                LocalDate.of(2024, 1, 15), LocalDate.of(2024, 5, 15),
                2024, Semester.SemesterType.SPRING, 1L));
        semesterRepository.save(TestDataBuilder.createSemester("Summer 2024", "SUMMER-2024",
                LocalDate.of(2024, 6, 1), LocalDate.of(2024, 8, 15),
                2024, Semester.SemesterType.SUMMER, 2L));
        flush();
        clearContext();

        long count = semesterRepository.countByProgramId(1L);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldClearCurrentSemesterFlag() {
        // Given
        Semester currentSemester = TestDataBuilder.createCurrentSemester();
        semesterRepository.save(currentSemester);
        flush();
        clearContext();

        // When
        semesterRepository.clearCurrentSemesterFlag(currentSemester.getProgramId());
        flush();
        clearContext();

        // Then
        Optional<Semester> found = semesterRepository.findById(currentSemester.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getIsCurrent()).isFalse();
    }

    @Test
    void shouldCheckExistsByCodeIgnoreCase() {
        // Given
        semesterRepository.save(testSemester);
        flush();
        clearContext();

        // When/Then
        assertThat(semesterRepository.existsByCodeIgnoreCase("fall-2024")).isTrue();
        assertThat(semesterRepository.existsByCodeIgnoreCase("FALL-2024")).isTrue();
        assertThat(semesterRepository.existsByCodeIgnoreCase("nonexistent")).isFalse();
    }

    @Test
    void shouldDeleteSemester() {
        // Given
        Semester saved = semesterRepository.save(testSemester);
        Long id = saved.getId();
        flush();
        clearContext();

        // When
        semesterRepository.deleteById(id);

        // Then
        assertThat(semesterRepository.findById(id)).isEmpty();
    }

    @Test
    void shouldUpdateSemester() {
        // Given
        Semester saved = semesterRepository.save(testSemester);
        flush();
        clearContext();

        // When
        Semester toUpdate = semesterRepository.findById(saved.getId()).orElseThrow();
        toUpdate.setName("Updated Semester Name");
        toUpdate.setDescription("Updated Description");
        semesterRepository.save(toUpdate);
        flush();
        clearContext();

        // Then
        Optional<Semester> found = semesterRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Semester Name");
        assertThat(found.get().getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void shouldUpdateSemesterStatus() {
        // Given
        Semester saved = semesterRepository.save(testSemester);
        flush();
        clearContext();

        // When
        semesterRepository.updateSemesterStatus(saved.getId(), Semester.SemesterStatus.ACTIVE);
        flush();
        clearContext();

        // Then
        Semester updated = semesterRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Semester.SemesterStatus.ACTIVE);
    }

    @Test
    void shouldSearchByAcademicYear() {
        semesterRepository.save(testSemester);
        semesterRepository.save(TestDataBuilder.createSemester("Fall 2023", "FALL-2023",
                LocalDate.of(2023, 9, 1), LocalDate.of(2023, 12, 15),
                2023, Semester.SemesterType.FALL, 1L));
        flush();
        clearContext();

        List<Semester> found = semesterRepository.searchSemesters(null, null, 2024, null, null, null, null, null);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getAcademicYear()).isEqualTo(2024);
    }

    @Test
    void shouldSearchByNameFragment() {
        semesterRepository.save(testSemester);
        semesterRepository.save(TestDataBuilder.createSemester("Spring 2024", "SPRING-2024",
                LocalDate.of(2024, 1, 15), LocalDate.of(2024, 5, 15),
                2024, Semester.SemesterType.SPRING, 1L));
        flush();
        clearContext();

        List<Semester> found = semesterRepository.searchSemesters(null, null, null, null, null, null, "Fall", null);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Fall 2024");
    }

    @Test
    void shouldReturnAllSemestersWhenNoFilters() {
        semesterRepository.save(testSemester);
        semesterRepository.save(TestDataBuilder.createSemester("Spring 2024", "SPRING-2024",
                LocalDate.of(2024, 1, 15), LocalDate.of(2024, 5, 15),
                2024, Semester.SemesterType.SPRING, 1L));
        flush();
        clearContext();

        List<Semester> found = semesterRepository.searchSemesters(null, null, null, null, null, null, null, null);

        assertThat(found).hasSize(2);
    }
}
