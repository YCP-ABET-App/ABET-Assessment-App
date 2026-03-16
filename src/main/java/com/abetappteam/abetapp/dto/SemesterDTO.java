package com.abetappteam.abetapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class SemesterDTO {

    private Long id;

    @NotBlank(message = "Semester name is required")
    @Size(max = 50, message = "Semester name must not exceed 50 characters")
    private String name;

    @NotBlank(message = "Semester code is required")
    @Size(max = 20, message = "Semester code must not exceed 20 characters")
    private String code;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Academic year is required")
    private Integer academicYear;

    @NotNull(message = "Semester type is required")
    private String type; // FALL, SPRING, SUMMER, WINTER

    @NotNull(message = "Program ID is required")
    private Long programId;

    private String description;
    private Boolean isCurrent;


    public SemesterDTO(String name, String code, LocalDate startDate,
        LocalDate endDate, Integer academicYear, String type, Long programId, String description, Boolean isCurrent){
            this.name = name;
            this.code = code;
            this.startDate = startDate;
            this.endDate = endDate;
            this.academicYear = academicYear;
            this.type = type;
            this.programId = programId;
            this.description = description;
            this.isCurrent = isCurrent;
    }
}