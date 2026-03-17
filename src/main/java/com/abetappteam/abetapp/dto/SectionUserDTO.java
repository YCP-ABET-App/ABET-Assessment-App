package com.abetappteam.abetapp.dto;
import jakarta.validation.constraints.NotBlank;


public class SectionUserDTO {

    @NotBlank(message = "Section ID is required")
    private final int sectionId;
    @NotBlank(message = "User ID is required")
    private final int userId;

    public SectionUserDTO(
            int sectionId,
            int userId,
            Boolean isActive
    ) {
        this.sectionId = sectionId;
        this.userId = userId;
    }

    // Getters and Setters
    public int getSectionId() {return sectionId;}
    public int getUserId() {return userId;}

    @Override
    public String toString() {
        return "SectionDTO{" +
                ", sectionId=" + sectionId +
                ", userId=" + userId +
                '}';
    }
}
