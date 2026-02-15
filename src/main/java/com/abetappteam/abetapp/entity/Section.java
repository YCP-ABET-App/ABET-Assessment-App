package com.abetappteam.abetapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Entity representing a Section in the ABET app
 * Sections organize course data by splitting up 
 */
@Entity
@Table(name = "section")
public class Section extends BaseEntity {

    // @NotBlank(message = "Semester code is required")
    // @Size(max = 20, message = "Semester code must not exceed 20 characters")
    // @Column(nullable = false, unique = true, length = 20)
    // private String code; // e.g., "FALL-2025"

    // @Enumerated(EnumType.STRING)
    // @Column(nullable = false, length = 10)
    // private SemesterType type; // FALL, SPRING, SUMMER, WINTER

    // @Enumerated(EnumType.STRING)
    // @Column(nullable = false, length = 15)
    // private SemesterStatus status = SemesterStatus.UPCOMING;

    // @Column(name = "start_date")
    // private LocalDate startDate;

    // @Column(name = "end_date")
    // private LocalDate endDate;

    // @Column(name = "academic_year")
    // private Integer academicYear;

    // @Column(length = 500)
    // private String description;


    @NotBlank(message = "Section name is required")
    @Size(max = 50, message = "Section name must not exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String name;
    
    @NotNull(message = "Course is required")
    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @NotNull(message = "Instructor is required")
    @Column(name = "instructor_id", nullable = false)
    private Long instructorId;

    @NotNull(message = "Semester is required")
    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;

    
    // Constructors
    public Section() {
    }

    public Section(String name, Long courseId, Long instructorId, Long semesterId) {

        this.name = name;
        this.courseId = courseId;
        this.instructorId = instructorId;
        this.semesterId = semesterId;
        this.isCurrent = false;
    }

    // Enum definitions
    public enum SemesterType {
        FALL, SPRING, SUMMER, WINTER
    }

    // dont need section status because it will know the semester which will know the status

    // public enum SemesterStatus {
    //     UPCOMING, // Semester hasn't started yet
    //     ACTIVE, // Semester is currently ongoing
    //     COMPLETED, // Semester has ended but assessments may be in progress
    //     ARCHIVED // Semester and all assessments are finalized
    // }

    // Business logic methods
    // public boolean canAddCourses() {
    //     return status == SemesterStatus.UPCOMING || status == SemesterStatus.ACTIVE;
    // }

    // public boolean canGenerateAssessment() {
    //     return status == SemesterStatus.COMPLETED || status == SemesterStatus.ACTIVE;
    // }

    // public boolean isEditable() {
    //     return status == SemesterStatus.UPCOMING || status == SemesterStatus.ACTIVE;
    // }

    // public boolean isActive() {
    //     return status == SemesterStatus.ACTIVE;
    // }

    // public boolean isCompleted() {
    //     return status == SemesterStatus.COMPLETED || status == SemesterStatus.ARCHIVED;
    // }


    // Getters and setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Long getCourseId() {
        return courseId;
    }
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getInstructorId() {
        return instructorId;
    }
    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }

    public Long getSemesterId() {
        return semesterId;
    }
    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }
    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }


    @Override
    public String toString() {
        return "Section{" +
                "id=" + getId() +
                ", name='" + name +
                ", courseId='" + courseId + '\'' +
                ", instructorId=" + instructorId +
                ", semesterId=" + semesterId +
                ", isCurrent=" + isCurrent +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}