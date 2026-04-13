package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Course entity
 * Based on schema: course table with fields (id, course_code, course_name, course_description, student_count, created_at, is_active)
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByIsActive(Boolean isActive);

    // ========== Course code queries ==========
    Optional<Course> findByCourseCodeIgnoreCase(String courseCode);

    boolean existsByCourseCodeIgnoreCase(String courseCode);

    List<Course> findByCourseCode(String courseCode);

    // ========== Course name queries ==========
    List<Course> findByCourseNameContainingIgnoreCase(String nameFragment);

    // ========== Instructor relationship queries (via course_instructor table) ==========
    // Note: These queries use the course_instructor junction table
    @Query("SELECT c FROM Course c JOIN CourseInstructor ci ON c.id = ci.courseId WHERE ci.programUserId = :programUserId AND c.isActive = true")
    List<Course> findActiveCoursesByProgramUserId(@Param("programUserId") Long programUserId);

    @Query("SELECT DISTINCT c FROM Course c JOIN CourseInstructor ci ON c.id = ci.courseId JOIN ProgramUser pu ON pu.id = ci.programUserId WHERE pu.programId = :programId AND pu.isActive = true AND c.isActive = true")
    List<Course> findActiveCoursesByProgramId(@Param("programId") Long programId);

    @Query("SELECT DISTINCT c FROM Course c WHERE " +
           "(:id IS NULL OR c.id = :id) AND " +
           "(:courseCode IS NULL OR c.courseCode = :courseCode) AND " +
           "(:courseName IS NULL OR LOWER(c.courseName) LIKE LOWER(CONCAT('%', :courseName, '%'))) AND " +
           "(:courseDescription IS NULL OR LOWER(c.courseDescription) LIKE LOWER(CONCAT('%', :courseDescription, '%'))) AND " +
           "(:studentCount IS NULL OR c.studentCount = :studentCount) AND " +
           "(:mirrorId IS NULL OR c.mirrorId = :mirrorId) AND " +
           "(:isActive IS NULL OR c.isActive = :isActive)")
        List<Course> searchCourse(
            @Param("id") Integer id,
            @Param("courseCode") String courseCode,
            @Param("courseName") String courseName,
            @Param("courseDescription") String courseDescription,
            @Param("studentCount") Integer studentCount,
            @Param("mirrorId") Integer mirrorId,
            @Param("isActive") boolean isActive
        );
    
    @Query("SELECT c FROM Course c JOIN CourseInstructor ci ON c.id = ci.courseId WHERE ci.programUserId = :programUserId")
    List<Course> findCoursesByProgramUserId(@Param("programUserId") Long programUserId);

    // ========== Methods for measure completeness calculations ==========
    // Based on schema: course -> course_indicator -> measure
    // Relationship: measure.courseIndicator_id -> course_indicator.id, course_indicator.course_id -> course.id
    @Query(value = "SELECT COUNT(m.id) FROM measure m " +
            "JOIN course_indicator ci ON m.course_indicator_id = ci.id " +
            "WHERE ci.course_id = :courseId AND m.is_active = true", nativeQuery = true)
    int countTotalMeasuresByCourseId(@Param("courseId") Long courseId);

    @Query(value = "SELECT COUNT(m.id) FROM measure m " +
            "JOIN course_indicator ci ON m.course_indicator_id = ci.id " +
            "WHERE ci.course_id = :courseId AND m.is_active = true " +
            "AND (m.met IS NOT NULL OR m.exceeded IS NOT NULL OR m.below IS NOT NULL)", nativeQuery = true)
    int countCompletedMeasuresByCourseId(@Param("courseId") Long courseId);

    @Query(value = "SELECT COUNT(m.id) FROM measure m " +
            "JOIN course_indicator ci ON m.course_indicator_id = ci.id " +
            "WHERE ci.course_id = :courseId AND m.is_active = true " +
            "AND m.met IS NULL AND m.exceeded IS NULL AND m.below IS NULL", nativeQuery = true)
    int countInProgressMeasuresByCourseId(@Param("courseId") Long courseId);

    @Query(value = "SELECT COUNT(m.id) FROM measure m " +
            "JOIN course_indicator ci ON m.course_indicator_id = ci.id " +
            "WHERE ci.course_id = :courseId AND m.is_active = true " +
            "AND m.fcar IS NOT NULL", nativeQuery = true)
    int countSubmittedMeasuresByCourseId(@Param("courseId") Long courseId);

    @Query(value = "SELECT COUNT(m.id) FROM measure m " +
            "JOIN course_indicator ci ON m.course_indicator_id = ci.id " +
            "WHERE ci.course_id = :courseId AND m.is_active = true " +
            "AND m.recommended_action IS NOT NULL", nativeQuery = true)
    int countMeasuresInReviewByCourseId(@Param("courseId") Long courseId);
}