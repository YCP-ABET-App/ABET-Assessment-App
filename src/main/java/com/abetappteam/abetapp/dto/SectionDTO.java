package com.abetappteam.abetapp.dto;

import jakarta.validation.constraints.NotBlank;

public final class SectionDTO {

    private final int id;

    @NotBlank(message = "Section number is required")
    private String sectionNumber;

    @NotBlank(message = "Course ID is required")
    private int courseId;

    @NotBlank(message = "Semester ID is required")
    private int semesterId;


    public SectionDTO(
            int id,
            String sectionNumber,
            int courseId,
            int semesterId,
            Boolean isActive
    ) {
        this.id = id;
        this.sectionNumber = sectionNumber;
        this.courseId = courseId;
        this.semesterId = semesterId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public String getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(String sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(int semesterId) {
        this.semesterId = semesterId;
    }


    @Override
    public String toString() {
        return "SectionDTO{" +
                "id=" + id +
                ", sectionNumber='" + sectionNumber + '\'' +
                ", courseId=" + courseId +
                ", semesterId=" + semesterId +
                '}';
    }
}
