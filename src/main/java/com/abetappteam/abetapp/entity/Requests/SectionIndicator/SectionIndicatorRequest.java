package com.abetappteam.abetapp.entity.Requests.SectionIndicator;

import java.util.List;

public record SectionIndicatorRequest(
        List<Integer> ids,
        List<Integer> sectionIds,
        List<Integer> indicatorIds

) {

}
