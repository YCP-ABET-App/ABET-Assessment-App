package com.abetappteam.abetapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "schedule_entry")
public class ScheduleEntry extends BaseEntity {

    @Positive(message = "Semester is required")
    @Column(name = "semester_id", nullable = false)
    private int semesterId;

    @Positive(message = "Course is required")
    @Column(name = "course_id", nullable = false)
    private int courseId;

    @Positive(message = "Program is required")
    @Column(name = "program_id", nullable = false)
    private int programId;

    @Positive(message = "Performance Indicator is required")
    @Column(name = "indicator_id", nullable = false)
    private int indicatorId;


    public ScheduleEntry(){super();}

    public ScheduleEntry(
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
    public void setSemesterId(int semesterId){this.semesterId = semesterId;}

    public int getCourseId(){return courseId;}
    public void setCourseId(int courseId){this.courseId = courseId;}

    public int getProgramId(){return programId;}
    public void setProgramId(int programId){this.programId = programId;}

    public int getIndicatorId(){return indicatorId;}
    public void setIndicatorId(int indicatorId){this.indicatorId = indicatorId;}


    @Override
    public String toString()
    {
        return "ScheduleEntry{" +
                "id=" + getId() +
                ", semesterId=" + semesterId +
                ", courseId=" + courseId +
                ", programId=" + programId +
                ", indicatorId=" + indicatorId +
                '}';
    }
}
