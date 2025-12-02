package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.entity.CourseIndicator;
import com.abetappteam.abetapp.repository.CourseIndicatorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CourseIndicatorService {

    @Autowired
    private CourseIndicatorRepository repository;

    public CourseIndicator getOrCreate(Long courseId, Long indicatorId) {
        Optional<CourseIndicator> existing =
                repository.findByCourseIdAndIndicatorId(courseId, indicatorId);

        if (existing.isPresent()) {
            return existing.get();
        }

        CourseIndicator ci = new CourseIndicator();
        ci.setCourseId(courseId);
        ci.setIndicatorId(indicatorId);
        ci.setIsActive(true);

        return repository.save(ci);
    }
}
