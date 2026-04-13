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
                        "WHERE se.indicatorId = :indicatorId " +
                        "AND se.semesterId = :semesterId " +
                        "AND (:active IS NULL OR m.active = :active)")
        List<Measure> searchMeasuresByIndicatorAndSemester(
                        @Param("indicatorId") Integer indicatorId,
                        @Param("semesterId") Integer semesterId,
                        @Param("active") Boolean active);

}
