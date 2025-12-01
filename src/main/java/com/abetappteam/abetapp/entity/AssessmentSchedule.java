package com.abetappteam.abetapp.entity;

import jakarta.persistence.*;

/**
 * Entity representing assessment schedule assignments
 * Maps which courses assess which outcomes in which academic years
 */
@Entity
@Table(name = "assessment_schedule")
public class AssessmentSchedule extends BaseEntity {

    @Column(name = "student_outcome_id", nullable = false)
    private Long studentOutcomeId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear; // e.g., "2024-2025"

    @Column(name = "program_id", nullable = false)
    private Long programId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Constructors
    public AssessmentSchedule() {
        super();
    }

    public AssessmentSchedule(Long studentOutcomeId, Long courseId, String academicYear, Long programId) {
        super();
        this.studentOutcomeId = studentOutcomeId;
        this.courseId = courseId;
        this.academicYear = academicYear;
        this.programId = programId;
        this.isActive = true;
    }

    // Getters and Setters
    public Long getStudentOutcomeId() {
        return studentOutcomeId;
    }

    public void setStudentOutcomeId(Long studentOutcomeId) {
        this.studentOutcomeId = studentOutcomeId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public Long getProgramId() {
        return programId;
    }

    public void setProgramId(Long programId) {
        this.programId = programId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "AssessmentSchedule{" +
                "id=" + getId() +
                ", studentOutcomeId=" + studentOutcomeId +
                ", courseId=" + courseId +
                ", academicYear='" + academicYear + '\'' +
                ", programId=" + programId +
                ", isActive=" + isActive +
                ", createdAt=" + getCreatedAt() +
                ", deleted=" + getDeleted() +
                '}';
    }
}