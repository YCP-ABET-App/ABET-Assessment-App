package com.abetappteam.abetapp.dto;
import jakarta.validation.constraints.NotBlank;

public final class SectionDTO {

    @NotBlank(message = "Section number is required")
    private String sectionNumber;

    @NotBlank(message = "Course ID is required")
    private final int courseId;

    @NotBlank(message = "Semester ID is required")
    private final int semesterId;


    public SectionDTO(

            String sectionNumber,
            int courseId,
            int semesterId,
            Boolean isActive
    ) {

        this.sectionNumber = sectionNumber;
        this.courseId = courseId;
        this.semesterId = semesterId;
    }

    // Getters and Setters

    public String getSectionNumber() {return sectionNumber;}
    public void setSectionNumber(String sectionNumber) {this.sectionNumber = sectionNumber;}

    public int getCourseId() {return courseId;}
    public int getSemesterId() {return semesterId;}


    @Override
    public String toString() {
        return "SectionDTO{" +
                ", sectionNumber='" + sectionNumber + '\'' +
                ", courseId=" + courseId +
                ", semesterId=" + semesterId +
                '}';
    }
}
