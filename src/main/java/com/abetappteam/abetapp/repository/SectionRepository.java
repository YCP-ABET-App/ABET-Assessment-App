package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.entity.Course;
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
    @Query("SELECT s FROM Section s WHERE s.semesterId = :semesterId")
    List<Course> searchBySemesterId(@Param("semesterId") Long semesterId);

    @Query("SELECT s FROM Section s WHERE s.courseId = :courseId")
    List<Course> searchByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT s FROM Section s WHERE s.semesterId = :#{#request.semesterId} AND s.courseId = :#{#request.courseId} AND s.sectionNumber LIKE %:#{#request.sectionNumber}%")
    Section searchSections(@Param("request")SectionSearchRequest request);

    @Query("SELECT s FROM Section s WHERE s.sectionNumber = :sectionNumber AND s.semesterId = :semesterId AND s.courseId = :courseId")
    boolean existsByCourseNumberAndSemesterIdAndCourseId(@Param("sectionNumber") String sectionNumber, @Param("semesterId") int semesterId, @Param("courseId") int courseId);
}
