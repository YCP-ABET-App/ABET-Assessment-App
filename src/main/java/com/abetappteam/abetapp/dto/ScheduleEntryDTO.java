package com.abetappteam.abetapp.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;

public class ScheduleEntryDTO {

    @NotBlank(message = "Semester is required")
    @Column(name = "semester_id", nullable = false)
    private final int semesterId;

    @NotBlank(message = "Course is required")
    @Column(name = "course_id", nullable = false)
    private final int courseId;

    @NotBlank(message = "Program is required")
    @Column(name = "program_id", nullable = false)
    private final int programId;

    @NotBlank(message = "Performance Indicator is required")
    @Column(name = "indicator_id", nullable = false)
    private final int indicatorId;


    public ScheduleEntryDTO(
            int semesterId,
            int courseId,
            int programId,
            int indicatorId
    ) {
        this.semesterId = semesterId;
        this.courseId = courseId;
        this.programId = programId;
        this.indicatorId = indicatorId;
    }

    public int getSemesterId(){return semesterId;}
    public int getCourseId(){return courseId;}
    public int getProgramId(){return programId;}
    public int getIndicatorId(){return indicatorId;}

}
