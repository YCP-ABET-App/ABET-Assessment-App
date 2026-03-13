package com.abetappteam.abetapp.entity.Requests.Semester;

import java.time.LocalDate;

public record SemesterSearchRequest(
        Integer id,
        String status,
        Integer academicYear,
        LocalDate startDate,
        LocalDate endDate,
        String type,
        String name,
        String code
) {

}
