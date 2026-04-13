package com.abetappteam.abetapp.entity.Requests.MeasureResults;

public record MeasureResultsSearchRequest(
        Integer id,
        Integer measureId,
        Integer sectionProgramId
) {
}
