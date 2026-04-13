package com.abetappteam.abetapp.entity.Requests.Course;

public record CourseSearchRequest(
        Integer id,
        String courseCode,
        String courseName,
        String courseDescription,
        Integer student_count,
        Integer mirrorId,
        Boolean isActive) {
}
