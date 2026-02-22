package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.entity.Semester;
import com.abetappteam.abetapp.entity.Semester.SemesterStatus;
import com.abetappteam.abetapp.entity.Semester.SemesterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Semester entity
 */
@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {

    @Query("SELECT s FROM Semester s WHERE " +
            "(:id IS NULL OR s.id = :id) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:academicYear IS NULL OR s.academicYear = :academicYear) AND " +
            "(:startDate IS NULL OR s.startDate >= :startDate) AND " +
            "(:endDate IS NULL OR s.endDate <= :endDate) AND " +
            "(:type IS NULL OR s.type = :type) AND " +
            "(:code IS NULL OR LOWER(s.code) LIKE LOWER(CONCAT('%', :code, '%')))" +
            "(:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<Semester> searchSemesters(
            @Param("id") int id,
            @Param("status") String status,
            @Param("academicYear") int academicYear,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") String type,
            @Param("name") String name,
            @Param("code") String code
    );

    boolean existsByCodeIgnoreCase(String code);

    // Current semester queries
    List<Semester> findByIsCurrentTrue();

    @Query("SELECT s FROM Semester s WHERE s.programId = :programId AND s.isCurrent = true")
    Optional<Semester> findCurrentSemesterByProgram(@Param("programId") Long programId);

    // Search methods
    @Query("SELECT s FROM Semester s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Semester> searchByNameOrCode(@Param("searchTerm") String searchTerm);

    @Query("SELECT s FROM Semester s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Semester> searchByNameOrCode(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Date-based queries
    @Query("SELECT s FROM Semester s WHERE s.startDate <= :date AND s.endDate >= :date AND s.status = 'ACTIVE'")
    List<Semester> findActiveSemestersOnDate(@Param("date") LocalDate date);

    // Active and upcoming semesters
    @Query("SELECT s FROM Semester s WHERE s.programId = :programId AND (s.status = 'ACTIVE' OR s.status = 'UPCOMING') ORDER BY s.startDate")
    List<Semester> findActiveAndUpcomingSemestersByProgram(@Param("programId") Long programId);

    // Count methods
    long countByProgramId(Long programId);

    long countByProgramIdAndStatus(Long programId, SemesterStatus status);

    // Bulk update methods
    @Modifying
    @Query("UPDATE Semester s SET s.isCurrent = false WHERE s.programId = :programId")
    void clearCurrentSemesterFlag(@Param("programId") Long programId);

    @Modifying
    @Query("UPDATE Semester s SET s.status = :status WHERE s.id = :semesterId")
    void updateSemesterStatus(@Param("semesterId") Long semesterId, @Param("status") SemesterStatus status);
}