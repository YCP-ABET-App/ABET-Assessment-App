package com.abetappteam.abetapp.dto;
import jakarta.validation.constraints.NotBlank;

public class SectionProgramDTO {

    @NotBlank(message = "Section ID is required")
    private final int sectionId;

    @NotBlank(message = "Program ID is required")
    private final int programId;


    public SectionProgramDTO(
            int sectionId,
            int programId,
            Boolean isActive
    ) {
        this.sectionId = sectionId;
        this.programId = programId;
    }

    // Getters and Setters
    public int getSectionId() {return sectionId;}
    public int getProgramId() {return programId;}


    @Override
    public String toString() {
        return "SectionDTO{" +
                ", sectionId=" + sectionId +
                ", programId=" + programId +
                '}';
    }
}
