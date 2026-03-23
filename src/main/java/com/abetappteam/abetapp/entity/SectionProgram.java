package com.abetappteam.abetapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "section_program")
public class SectionProgram extends BaseEntity {

    // @NotBlank(message = "Section is required")
    @Column(name = "section_id", nullable = false)
    private int sectionId;

    // @NotBlank(message = "Program is required")
    @Column(name = "program_id", nullable = false)
    private int programId;

    public SectionProgram() {
        super();
    }

    public SectionProgram(
            int sectionId,
            int programId) {
        this.sectionId = sectionId;
        this.programId = programId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public int getSectionId() {
        return this.sectionId;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public int getProgramId() {
        return this.programId;
    }

    @Override
    public String toString() {
        return "SectionProgram{" +
                "id=" + getId() +
                ", sectionId=" + sectionId +
                ", programId=" + programId +
                '}';
    }

}