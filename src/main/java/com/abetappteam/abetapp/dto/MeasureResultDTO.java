package com.abetappteam.abetapp.dto;

public class MeasureResultDTO {

    private final Long measureId;
    private final Long sectionProgramId;
    private Integer met;
    private Integer exceeded;
    private Integer below;
    private String observation;
    private String status;
    private String rejectionNote;

    // Constructors

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

    public Long getSectionProgramId() {
        return sectionProgramId;
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