package com.abetappteam.abetapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "measure")
public class Measure extends BaseEntity {

    @Column(name = "schedule_entry_id", nullable = false)
    private Long scheduleEntryId;

    // Description (required)
    @NotBlank(message = "Description of Measure is required")
    @Column(name = "measure_description", nullable = false, length = 3000)
    private String description;

    @Column(name = "recommended_action", length = 3000)
    private String recommendedAction;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "course_indicator_id", nullable = true)
    private Long courseIndicatorId;

    @Column(name = "semester_id", nullable = true)
    private Long semesterId;
    // ------------------------------------------
    // Constructors
    // ------------------------------------------

    public Measure() {
        super();
    }

    public Measure(
            Long scheduleEntryId,
            String description,
            String recommendedAction,
            Long courseIndicatorId,
            Long semesterId,
            Boolean active
    ) {
        this.scheduleEntryId = scheduleEntryId;
        this.description = description;
        this.recommendedAction = recommendedAction;
        this.courseIndicatorId = courseIndicatorId;
        this.semesterId = semesterId;
        this.active = active;
    }

    // ------------------------------------------
    // Getters & Setters
    // ------------------------------------------
    public Long getScheduleEntryId() { return scheduleEntryId; }
    public void setScheduleEntryId(Long scheduleEntryId) { this.scheduleEntryId = scheduleEntryId; }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }
    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getCourseIndicatorId() {
        return courseIndicatorId;
    }
    public void setCourseIndicatorId(Long courseIndicatorId) {
        this.courseIndicatorId = courseIndicatorId;
    }

    public Long getSemesterId() {
        return semesterId;
    }
    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }
    // ------------------------------------------
    // toString()
    // ------------------------------------------

    @Override
    public String toString() {
        return "Measure{" +
                "id=" + getId() +
                ", scheduleEntryId='" + scheduleEntryId + '\'' +
                ", description='" + description + '\'' +
                ", recommendedAction='" + recommendedAction + '\'' +
                ", active=" + active +
                ", courseIndicatorId=" + courseIndicatorId +
                ", semesterId=" + semesterId +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}
