package com.abetappteam.abetapp.dto.importer;

import java.util.List;

public class CourseImportDTO {
    private String courseCode;
    private List<MeasureImportDTO> measures;

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public List<MeasureImportDTO> getMeasures() { return measures; }
    public void setMeasures(List<MeasureImportDTO> measures) { this.measures = measures; }
}
