package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.entity.MeasureResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface MeasureResultRepository extends JpaRepository<MeasureResult, Long> {

    // Measure queries
    List<MeasureResult> findByMeasureId(Long measureId);

    Page<MeasureResult> findByMeasureId(Long measureId, Pageable pageable);

    long countByMeasureId(Long measureId);

    // Status queries
    List<MeasureResult> findByMeasureIdAndStatus(Long measureId, String status);

    List<MeasureResult> findByStatus(String status);

    Page<MeasureResult> findByMeasureIdAndStatus(Long measureId, String status, Pageable pageable);

    long countByMeasureIdAndStatus(Long measureId, String status);

    // Find specific relationship
    Optional<MeasureResult> findByMeasureIdAndSectionId(Long measureId, Long sectionId);

    Optional<MeasureResult> findByMeasureIdAndProgramId(Long measureId, Long programId);

    // Check existence
    boolean existsByMeasureIdAndSectionId(Long measureId, Long sectionId);

    boolean existsByMeasureIdAndProgramId(Long measureId, Long programId);

    // Delete operations
    void deleteByMeasureId(Long measureId);

    void deleteByProgramId(Long programId);

    void deleteByMeasureIdAndProgramId(Long measureId, Long programId);

    // search queries
    @Query("SELECT mr FROM MeasureResult mr WHERE mr.status NOT IN ('Rejected')")
    List<MeasureResult> findByMeasureIdAndActiveTrue(@Param("measureId") Long measureId);

    @Query("SELECT mr FROM MeasureResult mr WHERE mr.status = 'Rejected' AND mr.measureId = :measureId")
    List<MeasureResult> findByMeasureIdAndActiveFalse(@Param("measureId") Long measureId);

    @Query("SELECT mr FROM MeasureResult mr WHERE mr.status NOT IN ('Rejected') AND mr.programId = :programId")
    List<MeasureResult> findByProgramIdAndActiveTrue(@Param("programId") Long programId);

    @Query("SELECT mr FROM MeasureResult mr WHERE mr.status NOT IN ('Rejected') AND mr.sectionId = :sectionId")
    List<MeasureResult> findBySectionIdAndActiveTrue(@Param("sectionId") Long sectionId);

    @Query("SELECT mr FROM MeasureResult mr WHERE mr.status NOT IN ('Rejected') AND mr.status = :status")
    List<MeasureResult> findByActiveTrueAndStatus(@Param("status") String status);

    // Check if measure results has active sections
    @Query("SELECT COUNT(mr) > 0 FROM MeasureResult mr WHERE mr.sectionId = :sectionId AND mr.status NOT IN ('Rejected')")
    boolean hasActiveSections(@Param("sectionId") Long sectionId);

    // Check if measure results has active programs
    @Query("SELECT COUNT(mr) > 0 FROM MeasureResult mr WHERE mr.programId = :programId AND mr.status NOT IN ('Rejected')")
    boolean hasActivePrograms(@Param("programId") Long programId);
}