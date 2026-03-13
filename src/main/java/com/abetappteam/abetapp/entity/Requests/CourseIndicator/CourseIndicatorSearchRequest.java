package com.abetappteam.abetapp.entity.Requests.CourseIndicator;

public record CourseIndicatorSearchRequest(
        Integer courseId,
        Integer indicatorId,
        Boolean isActive) {
}
