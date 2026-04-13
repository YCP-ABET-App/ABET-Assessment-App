package com.abetappteam.abetapp.service;

import java.util.List;
import java.util.ArrayList;

import com.abetappteam.abetapp.entity.Requests.Measure.MeasureSearchRequest;
import com.abetappteam.abetapp.entity.Requests.MeasureResults.MeasureResultsSearchRequest;
import com.abetappteam.abetapp.entity.Requests.Section.SectionSearchRequest;
import com.abetappteam.abetapp.entity.Requests.SectionProgram.SectionProgramSearchRequest;

import com.abetappteam.abetapp.repository.SectionProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abetappteam.abetapp.entity.Measure;
import com.abetappteam.abetapp.entity.MeasureResult;
import com.abetappteam.abetapp.entity.Section;
import com.abetappteam.abetapp.entity.SectionProgram;
import com.abetappteam.abetapp.entity.Course;
import com.abetappteam.abetapp.entity.ScheduleEntry;
import com.abetappteam.abetapp.entity.CourseIndicator;
import com.abetappteam.abetapp.dto.MeasureDTO;
import com.abetappteam.abetapp.dto.MeasureResultDTO;
import com.abetappteam.abetapp.repository.CourseIndicatorRepository;
import com.abetappteam.abetapp.repository.CourseRepository;
import com.abetappteam.abetapp.repository.MeasureRepository;

@Service
public class MeasureService extends BaseService<Measure, Long, MeasureRepository> {

    private final SectionService sectionService;
    private final ScheduleEntryService scheduleEntryService;
    private final SectionProgramService sectionProgramService;
    private final MeasureResultService measureResultService;
    private final MeasureRepository measureRepository;

    @Autowired
    public MeasureService(
            MeasureRepository repository,
            ScheduleEntryService scheduleEntryService,
            SectionProgramService sectionProgramService,
            SectionService sectionService,
            MeasureResultService measureResultService,
            MeasureRepository measureRepository
    ) {
        super(repository);
        this.sectionService = sectionService;
        this.scheduleEntryService = scheduleEntryService;
        this.sectionProgramService = sectionProgramService;
        this.measureResultService = measureResultService;
        this.measureRepository = measureRepository;
    }

    @Override
    protected String getEntityName() {
        return "Measure";
    }

    // Create new Measure
    @Transactional
    public Measure create(MeasureDTO dto) {

        // Create and store measure
        Measure measure = new Measure();
        measure.setScheduleEntryId(dto.getScheduleEntryId());
        measure.setDescription(dto.getDescription());
        measure.setRecommendedAction(dto.getRecommendedAction());
        measure.setActive(dto.getActive());

        measure = repository.saveAndFlush(measure);
        Long measureId = measure.getId();
        logger.info("Created Measure with ID: {}", measureId);

        // Retrieve the Course ID from the ScheduleEntry
        ScheduleEntry se = scheduleEntryService.findById(dto.getScheduleEntryId());

        int courseId = se.getCourseId();
        int programId = se.getProgramId();

        // Find relevant Sections of this Course
        SectionSearchRequest sectionSearchRequest = new SectionSearchRequest(
                null, null, null, courseId, null);
        List<Section> sections = sectionService.searchSections(sectionSearchRequest);

        // Find program specific sections
        for (Section section : sections) {
            SectionProgramSearchRequest spSearchRequest = new SectionProgramSearchRequest(
                            null, section.getId().intValue(), programId);
            List<SectionProgram> sectionPrograms = sectionProgramService.searchSectionProgram(spSearchRequest);

            // Create MeasureResults using ONLY sectionProgramId
            for (SectionProgram sp : sectionPrograms) {

                MeasureResultDTO resultDto = new MeasureResultDTO(
                        measureId,
                        sp.getId(),
                        null, null, null, null,
                        "InProgress",
                        null
                );

                measureResultService.create(resultDto);
            }
        }

        return measure;
    }

    public Measure findById(Long id) {
        return measureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Measure Not found with id: " + id));
    }
    // Update Existing Measure
    @Transactional
    public Measure update(Long id, MeasureDTO dto) {
        Measure measure = findById(id);

        measure.setScheduleEntryId(dto.getScheduleEntryId());
        measure.setDescription(dto.getDescription());
        measure.setRecommendedAction(dto.getRecommendedAction());

        if (dto.getActive() != null) {
            measure.setActive(dto.getActive());
        }

        logger.info("Updating measure: {}", id);
        return repository.save(measure);
    }

    // Activate Measure
    @Transactional
    public Measure activate(Long id) {
        Measure measure = findById(id);
        measure.setActive(true);
        logger.info("Activating Measure: {}", id);
        return repository.save(measure);
    }

    // Deactivate Measure
    @Transactional
    public Measure deactivate(Long id) {
        Measure measure = findById(id);
        measure.setActive(false);
        /*
         * // Retrieve the Course ID from the CourseIndicator
         * CourseIndicator ci =
         * courseIndicatorService.findById(measure.getCourseIndicatorId());
         * 
         * // Find relevant Sections
         * SectionSearchRequest sectionSearchRequest = new SectionSearchRequest(
         * null, null, null, ci.getCourseId().intValue(), null
         * );
         * List<Section> sections = sectionService.searchSections(sectionSearchRequest);
         * 
         * // Loop through Sections and deactivate measure results
         * for (Section section : sections) {
         * MeasureResultsSearchRequest measureResultsRequest = new
         * MeasureResultsSearchRequest(
         * null,
         * measure.getId().intValue(),
         * section.getId().intValue(),
         * null
         * );
         * List<MeasureResult> measureResults =
         * measureResultService.searchMeasureResults(measureResultsRequest);
         * for (MeasureResult measureResult: measureResults){
         * // TODO: measure results should have active field
         * //measureResult.setActive(false);
         * }
         * }
         */
        logger.info("Deactivaitng Measure: {}", id);
        return repository.save(measure);
    }

    @Transactional
    public List<Measure> searchMeasures(MeasureSearchRequest request) {
        logger.info("Fetching all measures");
        return repository.searchMeasures(request.id(), request.scheduleEntryId(),
                request.active());
    }

    @Transactional
    public Measure createFromImport(Measure m) {
        return repository.save(m);
    }
}
