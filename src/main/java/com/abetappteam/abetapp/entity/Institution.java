package com.abetappteam.abetapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "institution")
public class Institution extends BaseEntity {

    @NotBlank(message = "Name of the Institution is required")
    @Size(min = 1, max = 100, message = "The name of an Institution must be between 1 and 100 characters long")
    @Column(name = "institution_name", nullable = false, length = 100)
    private String name;

    @Column(name = "color")
    private Integer color;

    @Size(max = 20, message = "Logo must not exceed 20 characters")
    @Column(name = "logo", length = 20)
    private String logo;

    public Institution() {
        super();
    }

    public Institution(String name, Integer color, String logo) {
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

    @Override
    public String toString() {
        return "Institution{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", color=" + color +
                ", logo='" + logo + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}