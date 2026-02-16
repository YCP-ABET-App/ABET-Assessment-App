package com.abetappteam.abetapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entity representing a Course Section
 * A section belongs to a course, semester, and instructor
 */
@Entity
@Table(name = "section")
public class Section extends BaseEntity {

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

    // Constructors
    public Section() {
    }

    public Section(String name, Long courseId, Long instructorId, Long semesterId) {
        this.name = name;
        this.courseId = courseId;
        this.instructorId = instructorId;
        this.semesterId = semesterId;
    }

    // Business logic (optional but nice)

    public boolean belongsToCourse(Long courseId) {
        return this.courseId.equals(courseId);
    }

    public boolean belongsToSemester(Long semesterId) {
        return this.semesterId.equals(semesterId);
    }

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

    @Override
    public String toString() {
        return "Section{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", courseId=" + courseId +
                ", instructorId=" + instructorId +
                ", semesterId=" + semesterId +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}
