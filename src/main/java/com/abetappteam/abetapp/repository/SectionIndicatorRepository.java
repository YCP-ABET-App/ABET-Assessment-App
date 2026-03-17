package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.entity.SectionIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectionIndicatorRepository extends JpaRepository<SectionIndicator, Long> {

    @Query("SELECT DISTINCT si FROM SectionIndicator si " +
            "")
    List<SectionIndicator> searchSectionIndicators(
            @Param("ids") List<Integer> ids,
            @Param("sectionIds") List<Integer> sectionIds,
            @Param("indicatorIds") List<Integer> indicatorIds
    );
}
