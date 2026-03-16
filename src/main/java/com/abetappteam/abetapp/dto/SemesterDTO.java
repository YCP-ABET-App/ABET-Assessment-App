package com.abetappteam.abetapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class SemesterDTO {

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
    private final Long programId;

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


    // Getters and setters
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getCode() {return code;}
    public void setCode(String code) {this.code = code;}

    public LocalDate getStartDate() {return startDate;}
    public void setStartDate(LocalDate startDate) {this.startDate = startDate;}

    public LocalDate getEndDate() {return endDate;}
    public void setEndDate(LocalDate endDate) {this.endDate = endDate;}

    public Integer getAcademicYear() {return academicYear;}
    public void setAcademicYear(Integer academicYear) {this.academicYear = academicYear;}

    public String getType() {return type;}
    public void setType(String type) {this.type = type;}

    public Long getProgramId() {return programId;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    public Boolean getIsCurrent() {return isCurrent;}
    public void setIsCurrent(Boolean isCurrent) {this.isCurrent = isCurrent;}
}