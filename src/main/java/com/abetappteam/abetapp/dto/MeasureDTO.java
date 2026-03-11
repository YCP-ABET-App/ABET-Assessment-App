package com.abetappteam.abetapp.dto;

import jakarta.validation.constraints.NotBlank;

public class MeasureDTO {

    private final Long courseIndicatorId;

    @NotBlank(message = "Description of Measure is required")
    private String description;

    private String recAction;

    private String status;

    private Boolean active;

    //Constructor
    public MeasureDTO(Long id, Long courseIndicatorId, String description, String observation, String recAction, String fcar, 
    Integer met, Integer exceeded, Integer below, String status, Boolean active){

        this.courseIndicatorId = courseIndicatorId;
        this.description = description;
        this.recAction = recAction;
        this.status = status;
        this.active = active;
    }

    //Getters/setters

    public Long getCourseIndicatorId() {
        return courseIndicatorId;
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
