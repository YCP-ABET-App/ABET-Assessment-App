package com.abetappteam.abetapp.entity.Requests.Course;

public record CourseSearchRequest(
    int id,
    String courseCode, 
    String courseName, 
    String courseDescription,
    int student_count,
    int mirrorId,
    boolean isActive
) {}
