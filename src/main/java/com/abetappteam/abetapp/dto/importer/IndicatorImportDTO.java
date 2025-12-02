package com.abetappteam.abetapp.dto.importer;

import java.util.List;

public class IndicatorImportDTO {
    private double number;  // e.g., 1.1
    private List<CourseImportDTO> courses;

    public double getNumber() { return number; }
    public void setNumber(double number) { this.number = number; }

    public List<CourseImportDTO> getCourses() { return courses; }
    public void setCourses(List<CourseImportDTO> courses) { this.courses = courses; }
}
