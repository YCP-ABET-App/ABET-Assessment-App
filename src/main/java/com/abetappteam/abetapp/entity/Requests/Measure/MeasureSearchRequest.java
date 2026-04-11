package com.abetappteam.abetapp.entity.Requests.Measure;

public record MeasureSearchRequest(
        Integer id,
        Integer scheduleEntryId,
        Boolean active
) {

}
