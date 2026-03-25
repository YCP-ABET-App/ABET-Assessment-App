package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.entity.Requests.Section.SectionSearchRequest;
import com.abetappteam.abetapp.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Repository for Section entity
* */
@Repository
public interface SectionRepository  extends JpaRepository<Section, Long>
{
    // ========== Course ID queries ==========
    Page<Section> findByCourseId(int courseId, Pageable pageable);

    List<Section> findByCourseId(int courseId);

    long countByCourseId(int courseId);

    // ========== Semester ID queries ==========
    Page<Section> findBySemesterId(int semesterId, Pageable pageable);

    List<Section> findBySemesterId(int semesterId);

    long countBySemesterId(int semesterId);

    // ========== Search Queries ==========
    @Query("SELECT DISTINCT s FROM Section s " +
       "LEFT JOIN SectionProgram sp ON s.id = sp.sectionId " +
       "LEFT JOIN SectionUser su ON s.id = su.sectionId " +
       "WHERE (:#{#request.ids()} IS NULL OR s.id in :#{#request.ids()}) " +
       "AND (:#{#request.semesterId()} IS NULL OR s.semesterId = :#{#request.semesterId()}) " +
       "AND (:#{#request.courseId()} IS NULL OR s.courseId = :#{#request.courseId()}) " +
       "AND (:#{#request.programId()} IS NULL OR sp.programId = :#{#request.programId()}) " +
       "AND (:#{#request.userId()} IS NULL OR su.userId = :#{#request.userId()})")
    List<Section> searchSections(@Param("request") SectionSearchRequest request);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Section s WHERE s.sectionNumber = :sectionNumber AND s.semesterId = :semesterId AND s.courseId = :courseId")
    boolean existsByCourseNumberAndSemesterIdAndCourseId(@Param("sectionNumber") String sectionNumber, @Param("semesterId") int semesterId, @Param("courseId") int courseId);
}
