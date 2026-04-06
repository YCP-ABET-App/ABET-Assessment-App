package com.abetappteam.abetapp.entity.Requests.ScheduleEntry;

public record ScheduleEntrySearchRequest(
        Integer id,
        Integer semesterId,
        Integer courseId,
        Integer programId,
        Integer indicatorId
) {

}