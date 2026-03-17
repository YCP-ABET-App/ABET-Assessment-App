package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.entity.SectionIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectionIndicatorRepository extends JpaRepository<SectionIndicator, Long> {

    @Query("SELECT DISTINCT si FROM SectionIndicator si " +
            "WHERE (:ids IS NULL OR si.id IN :ids) " +
            "AND (:sectionIds IS NULL OR si.sectionId IN :sectionIds) " +
            "AND (:indicatorIds IS NULL OR si.indicatorId IN :indicatorIds)")
    List<SectionIndicator> searchSectionIndicators(
            @Param("ids") List<Integer> ids,
            @Param("sectionIds") List<Integer> sectionIds,
            @Param("indicatorIds") List<Integer> indicatorIds
    );
}
