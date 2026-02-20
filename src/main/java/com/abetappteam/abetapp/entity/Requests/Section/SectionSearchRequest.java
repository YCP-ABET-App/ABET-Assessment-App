package com.abetappteam.abetapp.entity.Requests.Section;

public record SectionSearchRequest(
        int id,
        int semesterId,
        int programId,
        int courseId,
        int userId
) {

}
