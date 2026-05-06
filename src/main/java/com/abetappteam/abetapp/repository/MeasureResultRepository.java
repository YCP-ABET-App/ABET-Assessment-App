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
        Optional<MeasureResult> findByMeasureIdAndSectionProgramId(Long measureId, Long sectionProgramId);

        // Check existence
        boolean existsByMeasureIdAndSectionProgramId(Long measureId, Long sectionProgramId);

        // Delete operations
        void deleteByMeasureId(Long measureId);

        void deleteBySectionProgramId(Long sectionProgramId);

        void deleteByMeasureIdAndSectionProgramId(Long measureId, Long sectionProgramId);

        @Modifying
        @Transactional
        @Query("DELETE FROM MeasureResult mr WHERE mr.measureId IN " +
                        "(SELECT m.id FROM Measure m WHERE m.scheduleEntryId = :scheduleEntryId)")
        void deleteByScheduleEntryId(@Param("scheduleEntryId") Long scheduleEntryId);

        @Query("SELECT mr FROM MeasureResult mr " +
                        "WHERE (:id IS NULL OR mr.id = :id) " +
                        "AND (:measureId IS NULL OR mr.measureId = :measureId) " +
                        "AND (:sectionProgramId IS NULL OR mr.sectionProgramId = :sectionProgramId) ")
        List<MeasureResult> searchMeasureResults(@Param("id") Integer id,
                        @Param("measureId") Integer measureId,
                        @Param("sectionProgramId") Integer sectionProgramId);

        // Check if measure results has active section & program
        @Query("SELECT COUNT(mr) > 0 FROM MeasureResult mr WHERE mr.sectionProgramId = :sectionProgramId AND mr.status NOT IN ('Rejected')")
        boolean hasActiveSectionPrograms(@Param("sectionProgramId") Long sectionProgramId);

        // // Check if measure results has active programs
        // @Query("SELECT COUNT(mr) > 0 FROM MeasureResult mr WHERE mr.programId =
        // :programId AND mr.status NOT IN ('Rejected')")
        // boolean hasActivePrograms(@Param("programId") Long programId);
}