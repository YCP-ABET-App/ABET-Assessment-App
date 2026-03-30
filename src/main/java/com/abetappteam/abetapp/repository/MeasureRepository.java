package com.abetappteam.abetapp.repository;

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
        void deleteByCourseIndicatorId(Long courseIndicatorId);

        @Query("SELECT m FROM Measure m WHERE " +
                        "(:id IS NULL OR m.id = :id) AND " +
                        "(:courseIndicatorId IS NULL OR m.courseIndicatorId = :courseIndicatorId) AND " +
                        "(:active IS NULL OR m.active = :active)")
        List<Measure> searchMeasures(
                        @Param("id") Integer id,
                        @Param("courseIndicatorId") Integer courseIndicatorId,
                        @Param("active") Boolean active);

}
