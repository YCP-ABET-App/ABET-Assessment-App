package com.abetappteam.abetapp.entity.Requests.MeasureResults;

public record MeasureResultsSearchRequest(
        int id,
        int measureId,
        int sectionId,
        int programId
) {
}
