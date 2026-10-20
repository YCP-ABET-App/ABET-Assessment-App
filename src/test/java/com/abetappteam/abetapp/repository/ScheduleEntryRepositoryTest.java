package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.BaseRepositoryTest;
import com.abetappteam.abetapp.entity.Requests.ScheduleEntry.ScheduleEntrySearchRequest;
import com.abetappteam.abetapp.entity.ScheduleEntry;
import com.abetappteam.abetapp.entity.Semester;
import com.abetappteam.abetapp.util.TestDataBuilder;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ScheduleEntryRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ScheduleEntryRepository scheduleEntryRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    private ScheduleEntry entry1;
    private ScheduleEntry entry2;

    @BeforeEach
    void setUp() {
        entry1 = entry(1, 10, 2, 3);
        entry2 = entry(1, 10, 2, 4);
    }

    @Test
    void shouldSaveAndRetrieveScheduleEntry() {
        ScheduleEntry saved = scheduleEntryRepository.save(entry1);
        clearContext();

        Optional<ScheduleEntry> found = scheduleEntryRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSemesterId()).isEqualTo(1);
        assertThat(found.get().getCourseId()).isEqualTo(10);
        assertThat(found.get().getProgramId()).isEqualTo(2);
        assertThat(found.get().getIndicatorId()).isEqualTo(3);
    }

    @Test
    void shouldRejectNonPositiveIds() {
        assertThatThrownBy(() -> scheduleEntryRepository.saveAndFlush(entry(0, 10, 2, 3)))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Semester is required");
    }

    @Test
    void shouldFindAndCountBySemesterId() {
        scheduleEntryRepository.save(entry1);
        scheduleEntryRepository.save(entry2);
        scheduleEntryRepository.save(entry(5, 10, 2, 3));

        List<ScheduleEntry> found = scheduleEntryRepository.findBySemesterId(1);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(ScheduleEntry::getSemesterId).containsOnly(1);
        assertThat(scheduleEntryRepository.countBySemesterId(1)).isEqualTo(2);
    }

    @Test
    void shouldFindAndCountByCourseId() {
        scheduleEntryRepository.save(entry1);
        scheduleEntryRepository.save(entry2);
        scheduleEntryRepository.save(entry(1, 20, 2, 3));

        List<ScheduleEntry> found = scheduleEntryRepository.findByCourseId(10);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(ScheduleEntry::getCourseId).containsOnly(10);
        assertThat(scheduleEntryRepository.countByCourseId(10)).isEqualTo(2);
    }

    @Test
    void shouldFindAndCountByProgramId() {
        scheduleEntryRepository.save(entry1);
        scheduleEntryRepository.save(entry(1, 11, 2, 5));
        scheduleEntryRepository.save(entry(1, 10, 9, 3));

        List<ScheduleEntry> found = scheduleEntryRepository.findByProgramId(2);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(ScheduleEntry::getProgramId).containsOnly(2);
        assertThat(scheduleEntryRepository.countByProgramId(2)).isEqualTo(2);
    }

    @Test
    void shouldFindAndCountByIndicatorId() {
        scheduleEntryRepository.save(entry1);
        scheduleEntryRepository.save(entry(2, 11, 4, 3));
        scheduleEntryRepository.save(entry2);

        List<ScheduleEntry> found = scheduleEntryRepository.findByIndicatorId(3);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(ScheduleEntry::getIndicatorId).containsOnly(3);
        assertThat(scheduleEntryRepository.countByIndicatorId(3)).isEqualTo(2);
    }

    @Test
    void shouldReturnTrueWhenSemesterAndProgramMatch() {
        scheduleEntryRepository.save(entry1);

        Boolean exists = scheduleEntryRepository.existsBySemesterIdAndProgramId(1, 2);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSemesterAndProgramDoNotMatch() {
        scheduleEntryRepository.save(entry1);

        Boolean exists = scheduleEntryRepository.existsBySemesterIdAndProgramId(1, 9);

        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnTrueWhenSemesterCourseAndProgramMatch() {
        scheduleEntryRepository.save(entry1);

        Boolean exists = scheduleEntryRepository.existsBySemesterIdAndCourseIdAndProgramId(1, 10, 2);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenCourseDoesNotMatch() {
        scheduleEntryRepository.save(entry1);

        Boolean exists = scheduleEntryRepository.existsBySemesterIdAndCourseIdAndProgramId(1, 99, 2);

        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnTrueWhenAllFourFieldsMatch() {
        scheduleEntryRepository.save(entry1);

        // Parameter order follows @Param: semesterId, courseId, indicatorId, programId.
        Boolean exists = scheduleEntryRepository
                .existsBySemesterIdAndCourseIdAndProgramIdAndIndicatorId(1, 10, 3, 2);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenIndicatorDoesNotMatch() {
        scheduleEntryRepository.save(entry1);

        Boolean exists = scheduleEntryRepository
                .existsBySemesterIdAndCourseIdAndProgramIdAndIndicatorId(1, 10, 99, 2);

        assertThat(exists).isFalse();
    }

    @Test
    void shouldSearchBySemesterId() {
        scheduleEntryRepository.save(entry1);
        scheduleEntryRepository.save(entry2);
        scheduleEntryRepository.save(entry(8, 10, 2, 3));

        ScheduleEntrySearchRequest request = new ScheduleEntrySearchRequest(null, 1, null, null, null);
        List<ScheduleEntry> found = scheduleEntryRepository.searchScheduleEntry(request);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(ScheduleEntry::getSemesterId).containsOnly(1);
    }

    @Test
    void shouldSearchByCourseId() {
        scheduleEntryRepository.save(entry1);
        scheduleEntryRepository.save(entry(1, 20, 2, 3));

        ScheduleEntrySearchRequest request = new ScheduleEntrySearchRequest(null, null, 10, null, null);
        List<ScheduleEntry> found = scheduleEntryRepository.searchScheduleEntry(request);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getCourseId()).isEqualTo(10);
    }

    @Test
    void shouldSearchByProgramId() {
        scheduleEntryRepository.save(entry1);
        scheduleEntryRepository.save(entry(1, 10, 9, 3));

        ScheduleEntrySearchRequest request = new ScheduleEntrySearchRequest(null, null, null, 2, null);
        List<ScheduleEntry> found = scheduleEntryRepository.searchScheduleEntry(request);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getProgramId()).isEqualTo(2);
    }

    @Test
    void shouldSearchByIndicatorId() {
        scheduleEntryRepository.save(entry1);
        scheduleEntryRepository.save(entry2);

        ScheduleEntrySearchRequest request = new ScheduleEntrySearchRequest(null, null, null, null, 3);
        List<ScheduleEntry> found = scheduleEntryRepository.searchScheduleEntry(request);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getIndicatorId()).isEqualTo(3);
    }

    @Test
    void shouldSearchById() {
        ScheduleEntry saved = scheduleEntryRepository.save(entry1);
        scheduleEntryRepository.save(entry2);

        ScheduleEntrySearchRequest request = new ScheduleEntrySearchRequest(
                saved.getId().intValue(), null, null, null, null);
        List<ScheduleEntry> found = scheduleEntryRepository.searchScheduleEntry(request);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(saved.getId());
    }

    @Test
    void shouldSearchByAllFilters() {
        ScheduleEntry saved = scheduleEntryRepository.save(entry1);
        scheduleEntryRepository.save(entry2);
        scheduleEntryRepository.save(entry(1, 10, 9, 3));

        ScheduleEntrySearchRequest request = new ScheduleEntrySearchRequest(
                saved.getId().intValue(), 1, 10, 2, 3);
        List<ScheduleEntry> found = scheduleEntryRepository.searchScheduleEntry(request);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(saved.getId());
    }

    @Test
    void shouldReturnAllWhenNoFilters() {
        scheduleEntryRepository.save(entry1);
        scheduleEntryRepository.save(entry2);

        ScheduleEntrySearchRequest request = new ScheduleEntrySearchRequest(null, null, null, null, null);
        List<ScheduleEntry> found = scheduleEntryRepository.searchScheduleEntry(request);

        assertThat(found).hasSize(2);
    }

    @Test
    void shouldDeleteByCourseId() {
        scheduleEntryRepository.save(entry1);
        scheduleEntryRepository.save(entry2);
        scheduleEntryRepository.save(entry(1, 20, 2, 3));
        flushAndClear();

        scheduleEntryRepository.deleteByCourseId(10);
        flushAndClear();

        assertThat(scheduleEntryRepository.findByCourseId(10)).isEmpty();
        assertThat(scheduleEntryRepository.findByCourseId(20)).hasSize(1);
    }

    @Test
    void shouldFindByProgramIdAndSemesterDateRange() {
        Semester inside = semesterRepository.save(TestDataBuilder.createSemester(
                "Schedule Fall", "SE-RANGE-IN",
                LocalDate.of(2024, 9, 1), LocalDate.of(2024, 12, 15),
                2024, Semester.SemesterType.FALL, 2L));
        Semester outside = semesterRepository.save(TestDataBuilder.createSemester(
                "Schedule Spring", "SE-RANGE-OUT",
                LocalDate.of(2024, 1, 15), LocalDate.of(2024, 5, 15),
                2024, Semester.SemesterType.SPRING, 2L));
        Semester otherProgramSemester = semesterRepository.save(TestDataBuilder.createSemester(
                "Schedule Other", "SE-RANGE-OTHER",
                LocalDate.of(2024, 9, 1), LocalDate.of(2024, 12, 15),
                2024, Semester.SemesterType.FALL, 9L));

        scheduleEntryRepository.save(entry(inside.getId().intValue(), 10, 2, 3));
        scheduleEntryRepository.save(entry(outside.getId().intValue(), 10, 2, 4));
        scheduleEntryRepository.save(entry(otherProgramSemester.getId().intValue(), 10, 9, 3));
        flushAndClear();

        List<ScheduleEntry> found = scheduleEntryRepository.findByProgramIdAndSemesterDateRange(
                2, LocalDate.of(2024, 8, 1), LocalDate.of(2024, 12, 31));

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getSemesterId()).isEqualTo(inside.getId().intValue());
        assertThat(found.get(0).getProgramId()).isEqualTo(2);
        assertThat(found.get(0).getIndicatorId()).isEqualTo(3);
    }

    private ScheduleEntry entry(int semesterId, int courseId, int programId, int indicatorId) {
        return new ScheduleEntry(semesterId, courseId, programId, indicatorId);
    }
}
