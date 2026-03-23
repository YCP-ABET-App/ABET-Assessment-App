package com.abetappteam.abetapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "section_program")
public class SectionProgram extends BaseEntity {

    @NotBlank(message = "Section is required")
    @Column(name = "section_id", nullable = false)
    private Integer sectionId;

    @NotBlank(message = "Program is required")
    @Column(name = "program_id", nullable = false)
    private Integer programId;

    public SectionProgram(){super();}

    public SectionProgram(
        int sectionId,
        int programId
    ) {
        this.sectionId = sectionId;
        this.programId = programId;
    }

    public void setSectionId(Integer sectionId){this.sectionId = sectionId;}
    public Integer getSectionId(){return this.sectionId;}
    public void setProgramId(Integer programId){this.programId = programId;}
    public Integer getProgramId(){return this.programId;}

    @Override
    public String toString()
    {
        return "SectionProgram{" +
                "id=" + getId() +
                ", sectionId=" + sectionId +
                ", programId=" + programId +
                '}';
    }

}