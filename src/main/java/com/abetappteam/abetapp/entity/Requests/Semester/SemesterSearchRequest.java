package com.abetappteam.abetapp.entity.Requests.Semester;

import java.time.LocalDate;

public record SemesterSearchRequest(
        int id,
        String status,
        int academicYear,
        LocalDate startDate,
        LocalDate endDate,
        String type,
        String name,
        String code
) {

}
