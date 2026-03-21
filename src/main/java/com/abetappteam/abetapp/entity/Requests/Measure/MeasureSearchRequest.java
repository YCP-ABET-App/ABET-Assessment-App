package com.abetappteam.abetapp.entity.Requests.Measure;

public record MeasureSearchRequest(
        Integer id,
        Integer courseIndicatorId,
        Boolean active
) {

}
