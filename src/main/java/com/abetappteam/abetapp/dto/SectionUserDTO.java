package com.abetappteam.abetapp.dto;
import jakarta.validation.constraints.NotBlank;

public class SectionUserDTO {
    private final int id;

    @NotBlank(message = "Section ID is required")
    private int sectionId;

    @NotBlank(message = "User ID is required")
    private int userId;


    public SectionUserDTO(
            int id,
            int sectionId,
            int userId,
            Boolean isActive
    ) {
        this.id = id;
        this.sectionId = sectionId;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() {return id;}

    public int getSectionId() {return sectionId;}
    public void setSectionId(int sectionId) {this.sectionId = sectionId;}

    public int getUserId() {return userId;}
    public void setUserId(int userId) {this.userId = userId;}


    @Override
    public String toString() {
        return "SectionDTO{" +
                "id=" + id +
                ", sectionId=" + sectionId +
                ", userId=" + userId +
                '}';
    }
}
