package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.entity.MeasureResult;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Modifying
    @Transactional
    @Query("DELETE FROM MeasureResult mr WHERE mr.measureId IN " +
            "(SELECT m.id FROM Measure m WHERE m.courseIndicatorId = :courseIndicatorId)")
    void deleteByCourseIndicatorId(@Param("courseIndicatorId") Long courseIndicatorId);

    @Query("SELECT mr FROM MeasureResult mr " +
            "WHERE (:id IS NULL OR mr.id = :id) " +
            "AND (:measureId IS NULL OR mr.measureId = :measureId) " +
            "AND (:sectionId IS NULL OR mr.sectionId = :sectionId) " +
            "AND (:programId IS NULL OR mr.programId = :programId)")
    List<MeasureResult> searchMeasureResults(@Param("id") Integer id,
            @Param("measureId") Integer measureId,
            @Param("sectionId") Integer sectionId,
            @Param("programId") Integer programId);

    // Check if measure results has active sections
    @Query("SELECT COUNT(mr) > 0 FROM MeasureResult mr WHERE mr.sectionId = :sectionId AND mr.status NOT IN ('Rejected')")
    boolean hasActiveSections(@Param("sectionId") Long sectionId);

    // Check if measure results has active programs
    @Query("SELECT COUNT(mr) > 0 FROM MeasureResult mr WHERE mr.programId = :programId AND mr.status NOT IN ('Rejected')")
    boolean hasActivePrograms(@Param("programId") Long programId);
}