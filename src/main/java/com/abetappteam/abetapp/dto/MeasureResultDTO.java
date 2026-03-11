package com.abetappteam.abetapp.dto;

public class MeasureResultDTO {

    private final Long measureId;
    private final Long sectionId;
    private final Long programId;
    private Integer met;
    private Integer exceeded;
    private Integer below;
    private String observation;
    private String status;
    private String rejectionNote;

    // Constructors

    public MeasureResultDTO(Long id, Long measureId, Long sectionId, Long programId, Integer met, Integer exceeded,
            Integer below, String observation, String status, String rejectionNote, Boolean active) {

        this.measureId = measureId;
        this.sectionId = sectionId;
        this.programId = programId;
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

    public Long getSectionId() {
        return sectionId;
    }

    public Long getProgramId() {
        return programId;
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