package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.entity.Requests.SectionProgram.SectionProgramSearchRequest;
import com.abetappteam.abetapp.entity.SectionProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Repository for Section Program entity
 */
@Repository
public interface SectionProgramRepository extends JpaRepository<SectionProgram, Long> {

    // ========== Section ID queries ==========
    Page<SectionProgram> findBySectionId(int sectionId, Pageable pageable);
    List<SectionProgram> findBySectionId(int sectionId);
    long countBySectionId(int sectionId);

    // ========== Program ID queries ==========
    Page<SectionProgram> findByProgramId(int programId, Pageable pageable);
    List<SectionProgram> findByProgramId(int programId);
    long countByProgramId(int programId);

    @Query("SELECT sp FROM SectionProgram sp " +
            "WHERE (:#{#request.id()} IS NULL OR sp.id = :#{#request.id()}) " +
            "AND (:#{#request.sectionId()} IS NULL OR sp.sectionId = :#{#request.sectionId()}) " +
            "AND (:#{#request.programId()} IS NULL OR sp.programId = :#{#request.programId()})")
    List<SectionProgram> searchSectionProgram(@Param("request") SectionProgramSearchRequest request);

    @Query("SELECT sp FROM SectionProgram sp WHERE sp.sectionId = :sectionId")
    List<SectionProgram> findBySectionId(@Param("sectionId") Long sectionId);

    @Query("SELECT sp FROM SectionProgram sp WHERE sp.programId = :programId")
    List<SectionProgram> findByProgramId(@Param("programId") Long programId);

    @Query("SELECT COUNT(sp) > 0 FROM SectionProgram sp WHERE sp.sectionId = :sectionId AND sp.programId = :programId")
    Boolean existsBySectionIdAndProgramId(@Param("sectionId") int sectionId, @Param("programId") int programId);

}
