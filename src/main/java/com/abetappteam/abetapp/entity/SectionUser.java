package com.abetappteam.abetapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "section_user")
public class SectionUser extends BaseEntity {

    @NotNull(message = "Section is required")
    @Column(name = "section_id", nullable = false)
    private int sectionId;

    @NotNull(message = "User is required")
    @Column(name = "user_id", nullable = false)
    private int userId;

    public SectionUser(){super();}

    public SectionUser(
        int sectionId,
        int userId
    ) {
        this.sectionId = sectionId;
        this.userId = userId;
    }

    public int getSectionId(){return sectionId;}
    public void setSectionId(int sectionId){this.sectionId = sectionId;}
    public int getUserId(){return userId;}
    public void setUserId(int userId){this.userId = userId;}

    @Override
    public String toString()
    {
        return "SectionProgram{" +
                "id=" + getId() +
                ", sectionId=" + sectionId +
                ", userId=" + userId +
                '}';
    }

}