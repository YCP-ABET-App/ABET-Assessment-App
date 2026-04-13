package com.abetappteam.abetapp.dto;

import jakarta.validation.constraints.NotBlank;

public class OutcomeDTO {

    private Integer number;
    private Integer value;
    @NotBlank(message = "Description of Student Outcome is required")
    private String description;
    private String evaluation;
    private final Long semesterId;
    private final Long programId;
    private Boolean active;

    //Constructor
    public OutcomeDTO(Integer number, String description, String evaluation, Long semesterId, Long programId, Boolean active){

        this.number = number;
        //this.value = value;
        this.description = description;
        this.evaluation = evaluation;
        this.semesterId = semesterId;
        this.programId = programId;
        this.active = active;
    }


    //Setters and Getters
    public Integer getNumber(){
        return number;
    }
    public void setNumber(Integer number){
        this.number = number;
    }

    public Integer getValue(){
        return value;
    }
    public void setValue(Integer value){
        this.value = value;
    }

    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }

    public String getEvaluation(){
        return evaluation;
    }
    public void setEvaluation(String evaluation){
        this.evaluation = evaluation;
    }

    public Long getSemesterId(){
        return semesterId;
    }

    public Long getProgramId(){
        return programId;
    }

    public Boolean getActive(){
        return active;
    }
    public void setActive(Boolean active){
        this.active = active;
    }
}
