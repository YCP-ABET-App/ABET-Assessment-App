package com.abetappteam.abetapp.entity.Requests.Section;

import java.util.List;

public record SectionSearchRequest(
        List<Integer> ids,
        Integer semesterId,
        Integer programId,
        Integer courseId,
        Integer userId
) {

}
