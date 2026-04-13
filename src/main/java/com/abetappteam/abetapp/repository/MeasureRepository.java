package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.entity.CourseInstructor;
import com.abetappteam.abetapp.entity.Measure;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface MeasureRepository extends JpaRepository<Measure, Long> {
        @Modifying
        @Transactional
        void deleteByScheduleEntryId(Long scheduleEntryId);
        List<Measure> findByScheduleEntryId(Long scheduleEntryId);

        @Query(value = "SELECT * FROM measure WHERE course_indicator_id = :courseIndicatorId AND is_active = TRUE", nativeQuery = true)
        List<Measure> findByCourseIndicatorId(@Param("courseIndicatorId") Long courseIndicatorId);


        @Query("SELECT m FROM Measure m WHERE " +
                        "(:id IS NULL OR m.id = :id) AND " +
                        "(:scheduleEntryId IS NULL OR m.scheduleEntryId = :scheduleEntryId) AND " +
                        "(:active IS NULL OR m.active = :active)")
        List<Measure> searchMeasures(
                        @Param("id") Integer id,
                        @Param("scheduleEntryId") Integer scheduleEntryId,
                        @Param("active") Boolean active);

        @Query("SELECT m FROM Measure m " +
                        "INNER JOIN ScheduleEntry se ON m.scheduleEntryId = se.id " +
                        "WHERE se.programId = :programId " +
                        "AND se.semesterId = :semesterId " +
                        "AND se.indicatorId = :indicatorId " +
                        "AND (:active IS NULL OR m.active = :active)")
        List<Measure> findMeasuresByProgramSemesterIndicator(
                        @Param("programId") Integer programId,
                        @Param("semesterId") Integer semesterId,
                        @Param("indicatorId") Integer indicatorId,
                        @Param("active") Boolean active);

        @Query("SELECT m FROM Measure m " +
                        "INNER JOIN ScheduleEntry se ON m.scheduleEntryId = se.id " +
                        "WHERE se.programId = :programId " +
                        "AND se.semesterId IN :semesterIds " +
                        "AND (:active IS NULL OR m.active = :active)")
        List<Measure> findMeasuresByProgramAndSemesters(
                        @Param("programId") Integer programId,
                        @Param("semesterIds") List<Integer> semesterIds,
                        @Param("active") Boolean active);

}
