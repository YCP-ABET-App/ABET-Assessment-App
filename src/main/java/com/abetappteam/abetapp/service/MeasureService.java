package com.abetappteam.abetapp.service;

import java.util.List;
import java.util.ArrayList;

import com.abetappteam.abetapp.entity.Requests.Measure.MeasureSearchRequest;
import com.abetappteam.abetapp.entity.Requests.Section.SectionSearchRequest;
import com.abetappteam.abetapp.entity.Requests.SectionProgram.SectionProgramSearchRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abetappteam.abetapp.entity.Measure;
import com.abetappteam.abetapp.entity.Section;
import com.abetappteam.abetapp.entity.SectionProgram;
import com.abetappteam.abetapp.entity.Course;
import com.abetappteam.abetapp.entity.CourseIndicator;
import com.abetappteam.abetapp.dto.MeasureDTO;
import com.abetappteam.abetapp.dto.MeasureResultDTO;
import com.abetappteam.abetapp.repository.CourseIndicatorRepository;
import com.abetappteam.abetapp.repository.CourseRepository;
import com.abetappteam.abetapp.repository.MeasureRepository;

@Service
public class MeasureService extends BaseService<Measure, Long, MeasureRepository>{

    private final CourseIndicatorRepository courseIndicatorRepository;
    private final CourseRepository courseRepository;
    private final SectionService sectionService;
    private final CourseIndicatorService courseIndicatorService;
    private final SectionProgramService sectionProgramService;
    private final MeasureResultService measureResultService;

    @Autowired
    public MeasureService(
            MeasureRepository repository,
            CourseIndicatorRepository courseIndicatorRepository,
            CourseRepository courseRepository,
            SectionService sectionService,
            CourseIndicatorService courseIndicatorService,
            SectionProgramService sectionProgramService,
            MeasureResultService measureResultService
    ) {
        super(repository);
        this.courseIndicatorRepository = courseIndicatorRepository;
        this.courseRepository = courseRepository;
        this.sectionService = sectionService;
        this.courseIndicatorService = courseIndicatorService;
        this.sectionProgramService = sectionProgramService;
        this.measureResultService = measureResultService;
    }

    @Override
    protected String getEntityName(){
        return "Measure";
    }

    //Create new Measure
    @Transactional
    public Measure create(MeasureDTO dto) {
        // 1. Create and Save the Measure first
        Measure measure = new Measure();
        measure.setCourseIndicatorId(dto.getCourseIndicatorId());
        measure.setSemesterId(dto.getSemesterId());
        measure.setDescription(dto.getDescription());
        measure.setRecommendedAction(dto.getRecommendedAction());
        measure.setActive(dto.getActive());

        // saveAndFlush pushes the insert to the DB immediately so we get the ID back
        measure = repository.saveAndFlush(measure);
        Long measureId = measure.getId();
        logger.info("Created Measure with ID: {}", measureId);

        // 2. Retrieve the Course ID from the CourseIndicator
        CourseIndicator ci = courseIndicatorService.findById(measure.getCourseIndicatorId());

        // 3. Find relevant Sections
        SectionSearchRequest sectionSearchRequest = new SectionSearchRequest(
            null, null, null, ci.getCourseId().intValue(), null
        );
        List<Section> sections = sectionService.searchSections(sectionSearchRequest);

        // 4. Loop through Sections and Programs to create Results
        for (Section section : sections) {
            SectionProgramSearchRequest spSearchRequest = new SectionProgramSearchRequest(
                null, section.getId().intValue(), null
            );
            List<SectionProgram> sectionPrograms = sectionProgramService.searchSectionProgram(spSearchRequest);

            for (SectionProgram sp : sectionPrograms) {
                MeasureResultDTO resultDto = new MeasureResultDTO(
                    measureId,          
                    section.getId(), 
                    sp.getProgramId().longValue(), 
                    null, null, null, null, 
                    "InProgress",        
                    null
                );
                measureResultService.create(resultDto);
            }
        }

        return measure;
    }

    //Update Existing Measure
    @Transactional
    public Measure update(Long id, MeasureDTO dto){
        Measure measure = findById(id);

        measure.setCourseIndicatorId(dto.getCourseIndicatorId());
        measure.setSemesterId(dto.getSemesterId());
        measure.setDescription(dto.getDescription());
        measure.setRecommendedAction(dto.getRecommendedAction());

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
        return repository.searchMeasures(request.id(), request.courseIndicatorId(), request.semesterId(), request.active());
    }

    @Transactional
    public Measure createFromImport(Measure m) {
        return repository.save(m);
    }
}
