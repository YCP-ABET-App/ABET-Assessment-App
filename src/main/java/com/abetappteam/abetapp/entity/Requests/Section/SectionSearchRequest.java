package com.abetappteam.abetapp.entity.Requests.Section;

public record SectionSearchRequest(
        Integer id,
        Integer semesterId,
        Integer programId,
        Integer courseId,
        Integer userId
) {

}
