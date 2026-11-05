package com.abetappteam.abetapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InstitutionDTO {

    @NotBlank(message = "Name of the Institution is required")
    @Size(min = 1, max = 100, message = "The name of an Institution must be between 1 and 100 characters long")
    private String name;

    private Integer color;

    @Size(max = 20, message = "Logo must not exceed 20 characters")
    private String logo;

    public InstitutionDTO() {
    }

    public InstitutionDTO(String name, Integer color, String logo) {
        this.name = name;
        this.color = color;
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
