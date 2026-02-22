package com.abetappteam.abetapp.dto;
import jakarta.validation.constraints.NotBlank;

public class SectionProgramDTO {
    private final int id;

    @NotBlank(message = "Section ID is required")
    private int sectionId;

    @NotBlank(message = "Program ID is required")
    private int programId;


    public SectionProgramDTO(
            int id,
            int sectionId,
            int programId,
            Boolean isActive
    ) {
        this.id = id;
        this.sectionId = sectionId;
        this.programId = programId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public int getProgramId() {
        return programId;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
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
