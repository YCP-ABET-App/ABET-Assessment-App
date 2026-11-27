package com.abetappteam.abetapp.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MeasureResultDTO {

    private Long measureId;
    private Long sectionProgramId;
    @JsonProperty("studentsMet")
    @JsonAlias({"met", "studentsMet"})
    private Integer met;
    @JsonProperty("studentsExceeded")
    @JsonAlias({"exceeded", "studentsExceeded"})
    private Integer exceeded;
    @JsonProperty("studentsBelow")
    @JsonAlias({"below", "studentsBelow"})
    private Integer below;
    private String observation;
    private String status;
    private String rejectionNote;

    public MeasureResultDTO() {
    }

    public MeasureResultDTO(Long measureId, Long sectionProgramId, Integer met, Integer exceeded,
            Integer below, String observation, String status, String rejectionNote) {

        this.measureId = measureId;
        this.sectionProgramId = sectionProgramId;
        this.met = met;
        this.exceeded = exceeded;
        this.below = below;
        this.observation = observation;
        this.status = status;
        this.rejectionNote = rejectionNote;
    }

    // Getters/Setters

    public Long getMeasureId() {
        return measureId;
    }

    public void setMeasureId(Long measureId) {
        this.measureId = measureId;
    }

    public Long getSectionProgramId() {
        return sectionProgramId;
    }

    public void setSectionProgramId(Long sectionProgramId) {
        this.sectionProgramId = sectionProgramId;
    }

    public Integer getStudentsMet() {
        return met;
    }
    public void setStudentsMet(Integer met) {
        this.met = met;
    }

    public Integer getStudentsExceeded() {
        return exceeded;
    }
    public void setStudentsExceeded(Integer exceeded) {
        this.exceeded = exceeded;
    }

    public Integer getStudentsBelow() {
        return below;
    }
    public void setStudentsBelow(Integer below) {
        this.below = below;
    }

    public String getObservation() {
        return observation;
    }
    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionNote() {
        return rejectionNote;
    }
    public void setRejectionNote(String rejectionNote) {
        this.rejectionNote = rejectionNote;
    }
}