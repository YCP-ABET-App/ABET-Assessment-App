package com.abetappteam.abetapp.dto.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Indicator with nested measures
 */
public class IndicatorReportData implements Serializable {
    private Long indicatorId;
    private String indicatorNumber;
    private String courseCode;
    private Integer studentCount;
    private List<ReportMeasureData> measures;

    public IndicatorReportData() {
        this.measures = new ArrayList<>();
    }

    public IndicatorReportData(Long indicatorId, String indicatorNumber, String courseCode, Integer studentCount) {
        this.indicatorId = indicatorId;
        this.indicatorNumber = indicatorNumber;
        this.courseCode = courseCode;
        this.studentCount = studentCount;
        this.measures = new ArrayList<>();
    }

    // Getters and Setters
    public Long getIndicatorId() {
        return indicatorId;
    }

    public void setIndicatorId(Long indicatorId) {
        this.indicatorId = indicatorId;
    }

    public String getIndicatorNumber() {
        return indicatorNumber;
    }

    public void setIndicatorNumber(String indicatorNumber) {
        this.indicatorNumber = indicatorNumber;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public List<ReportMeasureData> getMeasures() {
        return measures;
    }

    public void setMeasures(List<ReportMeasureData> measures) {
        this.measures = measures;
    }

    public void addMeasure(ReportMeasureData measure) {
        this.measures.add(measure);
    }
}
