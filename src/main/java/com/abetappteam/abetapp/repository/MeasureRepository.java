package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.entity.Measure;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface MeasureRepository extends JpaRepository<Measure, Long>{
        @Query("SELECT m FROM Measure m WHERE " +
                "(:id IS NULL OR m.id = :id) AND " +
                "(:courseIndicatorId IS NULL OR m.courseIndicatorId = :courseIndicatorId) AND " +
                "(:active IS NULL OR m.active = :active)")
        List<Measure> searchMeasures(
                @Param("id") Integer id,
                @Param("courseIndicatorId") Integer courseIndicatorId,
                @Param("semesterId") Integer semesterId,
                @Param("active") Boolean active
        );

}
