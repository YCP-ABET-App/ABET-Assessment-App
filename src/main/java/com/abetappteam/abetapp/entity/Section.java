package com.abetappteam.abetapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "section")
public class Section extends BaseEntity {

    @NotBlank(message = "Section Number is required")
    @Size(min = 1, max = 50, message = "The Section Number must be between 1 and 50 characters long")
    @Column(name = "section_number", nullable = false, length = 50)
    private String sectionNumber;

    // @NotBlank(message = "Course is required")
    @Column(name = "course_id", nullable = false)
    private int courseId;

    // @NotBlank(message = "Semester is required")
    @Column(name = "semester_id", nullable = false)
    private int semesterId;

    public Section() {
        super();
    }

    public Section(
            String sectionNumber,
            int courseId,
            int semesterId) {
        this.sectionNumber = sectionNumber;
        this.courseId = courseId;
        this.semesterId = semesterId;
    }

    // Getters and Setters
    public String getSectionNumber() {
        return sectionNumber;
    }

    public int getCourseId() {
        return courseId;
    }

    public int getSemesterId() {
        return semesterId;
    }

    public void setSectionNumber(String sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public void setSemesterId(int semesterId) {
        this.semesterId = semesterId;
    }

    @Override
    public String toString() {
        return "Section{" +
                " sectionNumber='" + sectionNumber + '\'' +
                ", courseId=" + courseId +
                ", semesterId=" + semesterId +
                '}';
    }

}
