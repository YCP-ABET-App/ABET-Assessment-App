package com.abetappteam.abetapp.entity.Requests.Measure;

import java.util.List;

public record MeasureSearchRequest(
        int id,
        int courseIndicatorId,
        boolean active
) {

}
