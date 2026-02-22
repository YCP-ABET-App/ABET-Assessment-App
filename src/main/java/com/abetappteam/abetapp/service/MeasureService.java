package com.abetappteam.abetapp.service;

import java.util.List;
import java.util.ArrayList;

import com.abetappteam.abetapp.entity.Requests.Measure.MeasureSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abetappteam.abetapp.entity.Measure;
import com.abetappteam.abetapp.entity.Course;
import com.abetappteam.abetapp.entity.CourseIndicator;
import com.abetappteam.abetapp.dto.MeasureDTO;
import com.abetappteam.abetapp.repository.CourseIndicatorRepository;
import com.abetappteam.abetapp.repository.CourseRepository;
import com.abetappteam.abetapp.repository.MeasureRepository;

@Service
public class MeasureService extends BaseService<Measure, Long, MeasureRepository>{
    
    private final CourseIndicatorRepository courseIndicatorRepository;
    private final CourseRepository courseRepository;
    
    @Autowired
    public MeasureService(MeasureRepository repository, CourseIndicatorRepository courseIndicatorRepository, CourseRepository courseRepository){
        super(repository);
        this.courseIndicatorRepository = courseIndicatorRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    protected String getEntityName(){
        return "Measure";
    }

    //Create new Measure
    @Transactional
    public Measure create(MeasureDTO dto){
        Measure measure = new Measure();
        measure.setId(dto.getId());
        measure.setCourseIndicatorId(dto.getCourseIndicatorId());
        measure.setDescription(dto.getDescription());
        measure.setFcar(dto.getFCar());
        measure.setRecommendedAction(dto.getRecommendedAction());
        measure.setStatus(dto.getStatus());
        measure.setActive(dto.getActive());

        logger.info("Creating new measure: {}", dto.getId());
        return repository.save(measure);
    }

    //Update Existing Measure
    @Transactional
    public Measure update(Long id, MeasureDTO dto){
        Measure measure = findById(id);

        measure.setId(dto.getId());
        measure.setCourseIndicatorId(dto.getCourseIndicatorId());
        measure.setDescription(dto.getDescription());
        measure.setFcar(dto.getFCar());
        measure.setRecommendedAction(dto.getRecommendedAction());
        measure.setStatus(dto.getStatus());
        if(dto.getActive() != null){
            measure.setActive(dto.getActive());
        }

        logger.info("Updating measure: {}", id);
        return repository.save(measure);
    }

    //Activate Measure
    @Transactional
    public Measure activate(Long id){
        Measure measure = findById(id);
        measure.setActive(true);
        logger.info("Activating Measure: {}", id);
        return repository.save(measure);
    }
    
    //Deactivate Measure
    @Transactional
    public Measure deactivate(Long id){
        Measure measure = findById(id);
        measure.setActive(false);
        logger.info("Deactivaitng Measure: {}", id);
        return repository.save(measure);
    }

    @Transactional
    public List<Measure> searchMeasures(MeasureSearchRequest request) {
        logger.info("Fetching all measures");
        return repository.searchMeasures(request.id(), request.courseIndicatorId(), request.active());
    }

    @Transactional
    public Measure createFromImport(Measure m) {
        return repository.save(m);
    }
}
