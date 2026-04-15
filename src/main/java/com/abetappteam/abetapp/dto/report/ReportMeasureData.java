package com.abetappteam.abetapp.dto.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight DTO combining Measure and MeasureResult data for reporting
 */
public class ReportMeasureData implements Serializable {
    private Long measureId;
    private Long courseIndicatorId;
    private String courseCode;
    private String description;
    private Integer studentsMet;
    private Integer studentsExceeded;
    private Integer studentsBelow;
    private Double metPercentage;
    private String status;
    private String note;
    private String recommendedAction;

    public ReportMeasureData() {
    }

    public ReportMeasureData(Long measureId, Long courseIndicatorId, String courseCode, String description,
            Integer studentsMet, Integer studentsExceeded, Integer studentsBelow,
            Double metPercentage, String status, String note, String recommendedAction) {
        this.measureId = measureId;
        this.courseIndicatorId = courseIndicatorId;
        this.courseCode = courseCode;
        this.description = description;
        this.studentsMet = studentsMet;
        this.studentsExceeded = studentsExceeded;
        this.studentsBelow = studentsBelow;
        this.metPercentage = metPercentage;
        this.status = status;
        this.note = note;
        this.recommendedAction = recommendedAction;
    }

    // Getters and Setters
    public Long getMeasureId() {
        return measureId;
    }

    public void setMeasureId(Long measureId) {
        this.measureId = measureId;
    }

    public Long getCourseIndicatorId() {
        return courseIndicatorId;
    }

    public void setCourseIndicatorId(Long courseIndicatorId) {
        this.courseIndicatorId = courseIndicatorId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Double getMetPercentage() {
        return metPercentage;
    }

    public void setMetPercentage(Double metPercentage) {
        this.metPercentage = metPercentage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }
}
