package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.entity.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Section entity
 */
@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    // Basic find methods

    Page<Section> findBySemesterId(Long semesterId, Pageable pageable);

    List<Section> findBySemesterId(Long semesterId);

    Page<Section> findByCourseId(Long courseId, Pageable pageable);

    List<Section> findByCourseId(Long courseId);

    Page<Section> findByInstructorId(Long instructorId, Pageable pageable);

    List<Section> findByInstructorId(Long instructorId);

    // Combined queries

    Page<Section> findBySemesterIdAndCourseId(Long semesterId, Long courseId, Pageable pageable);

    List<Section> findBySemesterIdAndCourseId(Long semesterId, Long courseId);

    // Unique section validation (important)

    Optional<Section> findByNameIgnoreCaseAndCourseIdAndSemesterId(
            String name,
            Long courseId,
            Long semesterId
    );

    boolean existsByNameIgnoreCaseAndCourseIdAndSemesterId(
            String name,
            Long courseId,
            Long semesterId
    );

    // Search functionality

    @Query("SELECT s FROM Section s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Section> searchByName(@Param("searchTerm") String searchTerm);

    @Query("SELECT s FROM Section s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Section> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Count methods

    long countBySemesterId(Long semesterId);

    long countByCourseId(Long courseId);

}
