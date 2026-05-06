package com.abetappteam.abetapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "measure_result")
public class MeasureResult extends BaseEntity {

    @Column(name = "measure_id", nullable = false)
    private Long measureId;

    @Column(name = "section_program_id", nullable = false)
    private Long sectionProgramId;

    @Column(name = "met")
    private Integer studentsMet;

    @Column(name = "exceeded")
    private Integer studentsExceeded;

    @Column(name = "below")
    private Integer studentsBelow;

    @Column(name = "observation", length = 200)
    private String observation;

    @Column(name = "rejection_note", length = 200)
    private String rejectionNote;

    @Column(name = "m_status", length = 10)
    private String status;

    // Constructors
    public MeasureResult() {
    }

    public MeasureResult(
            Long measureId,
            Long sectionProgramId,
            Integer studentsMet,
            Integer studentsExceeded,
            Integer studentsBelow,
            String observation,
            String rejectionNote,
            String status) {
        this.measureId = measureId;
        this.sectionProgramId = sectionProgramId;
        this.studentsMet = studentsMet;
        this.studentsExceeded = studentsExceeded;
        this.studentsBelow = studentsBelow;
        this.observation = observation;
        this.rejectionNote = rejectionNote;
        this.status = status;
    }

    // Getters and Setters
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
        return studentsMet;
    }
    public void setStudentsMet(Integer studentsMet) {
        this.studentsMet = studentsMet;
    }

    public Integer getStudentsExceeded() {
        return studentsExceeded;
    }
    public void setStudentsExceeded(Integer studentsExceeded) {
        this.studentsExceeded = studentsExceeded;
    }

    public Integer getStudentsBelow() {
        return studentsBelow;
    }
    public void setStudentsBelow(Integer studentsBelow) {
        this.studentsBelow = studentsBelow;
    }

    public String getObservation() {
        return observation;
    }
    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getRejectionNote() {
        return rejectionNote;
    }
    public void setRejectionNote(String rejectionNote) {
        this.rejectionNote = rejectionNote;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MeasureResult{" +
                "id=" + getId() +
                ", measureId=" + measureId +
                ", sectionProgramId=" + sectionProgramId +
                ", studentsMet=" + studentsMet +
                ", studentsExceeded=" + studentsExceeded +
                ", studentsBelow=" + studentsBelow +
                ", observation='" + observation + '\'' +
                ", rejectionNote='" + rejectionNote + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                ", version=" + getVersion() +
                ", deleted=" + getDeleted() +
                '}';
    }
}
