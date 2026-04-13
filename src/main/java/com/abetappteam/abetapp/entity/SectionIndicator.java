package com.abetappteam.abetapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "section_indicator")
public class SectionIndicator extends BaseEntity {

    // @NotBlank(message = "Section is required")
    @Column(name = "section_id", nullable = false)
    private int sectionId;

    // @NotBlank(message = "Indicator is required")
    @Column(name = "indicator_id", nullable = false)
    private int indicatorId;

    @Column(name = "complete")
    private boolean complete;

    public SectionIndicator() {
        super();
    }

    public SectionIndicator(
            int sectionId,
            int indicatorId,
            boolean complete) {
        this.sectionId = sectionId;
        this.indicatorId = indicatorId;
        this.complete = complete;
    }

    public int getSectionId() {
        return sectionId;
    }
    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public int getIndicatorId() {
        return indicatorId;
    }
    public void setIndicatorId(int indicatorId) {
        this.indicatorId = indicatorId;
    }

    public boolean getComplete() { return complete; }
    public void setComplete(boolean complete) {}

    @Override
    public String toString() {
        return "SectionIndicator{" +
                "id=" + getId() +
                ", sectionId=" + sectionId +
                ", indicatorId=" + indicatorId +
                ", complete=" + complete +
                '}';
    }

}