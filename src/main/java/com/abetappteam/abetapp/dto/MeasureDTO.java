package com.abetappteam.abetapp.dto;

import jakarta.validation.constraints.NotBlank;

public class MeasureDTO {
    private Long id;

    private Long courseIndicatorId;

    @NotBlank(message = "Description of Measure is required")
    private String description;


    private String recAction;

    private String fcar;

    private String status;

    private Boolean active;

    //Constructors
    public MeasureDTO(){
    }

    public MeasureDTO(Long id, Long courseIndicatorId, String description, String observation, String recAction, String fcar, 
    Integer met, Integer exceeded, Integer below, String status, Boolean active){
        this.id = id;
        this.courseIndicatorId = courseIndicatorId;
        this.description = description;
        this.recAction = recAction;
        this.fcar = fcar;
        this.status = status;
        this.active = active;
    }

    //Getters/setters
    public Long getId(){
        return this.id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public Long getCourseIndicatorId() {
        return courseIndicatorId;
    }

    public void setCourseIndicatorId(Long courseIndicatorId) {
        this.courseIndicatorId = courseIndicatorId;
    }
 
    public void setDescription(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

    public String getRecommendedAction(){
        return recAction;
    }

    public void setRecommendedAction(String recAction){
        this.recAction = recAction;
    }

    public String getFCar(){
        return fcar;
    }

    public void setFCar(String fcar){
        this.fcar = fcar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
 
    public Boolean getActive(){
        return active;
    }

    public void setActive(Boolean active){
        this.active = active;
    }

}
