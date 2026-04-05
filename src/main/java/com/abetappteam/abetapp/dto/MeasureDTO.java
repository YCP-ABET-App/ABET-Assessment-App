package com.abetappteam.abetapp.dto;

import jakarta.validation.constraints.NotBlank;

public class MeasureDTO {

    private final Long courseIndicatorId;
    private final Long sectionIndicatorId;
    private final Long semesterId;

    @NotBlank(message = "Description of Measure is required")
    private String description;
    private String recAction;
    private Boolean active;

    //Constructor
    public MeasureDTO(Long courseIndicatorId, Long sectionIndicatorId, Long semesterId, String description, String recAction,
    Boolean active){

        this.courseIndicatorId = courseIndicatorId;
        this.sectionIndicatorId = sectionIndicatorId;
        this.semesterId = semesterId;
        this.description = description;
        this.recAction = recAction;
        this.active = active;
    }

    //Getters/setters

    public Long getCourseIndicatorId() { return courseIndicatorId; }
    public Long getSectionIndicatorId() { return sectionIndicatorId; }
    public Long getSemesterId() { return semesterId; }
 
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
 
    public Boolean getActive(){
        return active;
    }
    public void setActive(Boolean active){
        this.active = active;
    }

}