package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.entity.Requests.Measure.MeasureSearchRequest;
import com.abetappteam.abetapp.entity.Requests.Section.SectionSearchRequest;
import com.abetappteam.abetapp.entity.Requests.SectionProgram.SectionProgramSearchRequest;
import com.abetappteam.abetapp.service.CourseIndicatorService;
import com.abetappteam.abetapp.service.MeasureResultService;
import com.abetappteam.abetapp.service.MeasureService;
import com.abetappteam.abetapp.service.SectionProgramService;
import com.abetappteam.abetapp.service.SectionService;
import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.dto.MeasureDTO;
import com.abetappteam.abetapp.dto.MeasureResultDTO;
import com.abetappteam.abetapp.entity.Measure;
import com.abetappteam.abetapp.entity.MeasureResult;
import com.abetappteam.abetapp.entity.Section;
import com.abetappteam.abetapp.entity.SectionProgram;
import com.abetappteam.abetapp.entity.CourseIndicator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/measure")
public class MeasureController extends BaseController{
    
    @Autowired
    private MeasureService service;
    @Autowired
    private SectionService sectionService;
    @Autowired
    private CourseIndicatorService courseIndicatorService;
    @Autowired
    private SectionProgramService sectionProgramService;
    @Autowired
    private MeasureResultService measureResultService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Measure>>> searchMeasures(
        @RequestParam(required = false) Integer id,
        @RequestParam(required = false) Integer courseIndicatorId,
        @RequestParam(required = false) Boolean active
    ) {
        MeasureSearchRequest request = new MeasureSearchRequest(id, courseIndicatorId, active);
        logger.info("Fetching measures for request: {}", request);
        List<Measure> measures = service.searchMeasures(request);
        return success(measures, "Measures retrieved successfully");
    }

    //Create a new measure
    @PostMapping
    public ResponseEntity<ApiResponse<Measure>> createMeasure(
        @Valid @RequestBody MeasureDTO dto
    ) {
        logger.info("Creating new measure: ", dto.getId());
        Measure measure = service.create(dto);
        Integer measure_id = measure.getId().intValue();

        //Retrieve course id
        CourseIndicator ci = courseIndicatorService.findById(measure.getCourseIndicatorId());

        //Retrieve a list of sections of that course for this semester
        SectionSearchRequest section_search_request = new SectionSearchRequest(
            null,
            null,
            null,
            ci.getCourseId().intValue(),
            null
        );
        List<Section> sections = sectionService.searchSections(section_search_request);

        //For each section detemine all of the associated programs
        for(Section section: sections){
            Long section_id = section.getId();
            SectionProgramSearchRequest sp_search_request = new SectionProgramSearchRequest(
                null,
                section.getId().intValue(),
                null
            );
            List<SectionProgram> section_programs = sectionProgramService.searchSectionProgram(sp_search_request);

            //Create a measure results object for each
            for(SectionProgram section_program: section_programs){
                MeasureResult measure_result = measureResultService.create(new MeasureResultDTO(
                    null,
                    (Long) measure_id.longValue(),
                    section_id,
                    (Long) section_program.getProgramId().longValue(),
                    null,
                    null,
                    null,
                    null,
                    "InProgress",
                    null,
                    true
                ));
                logger.info("Creating new Measure Result: ", measure_result.getId());
            }
        }

        //Return created measure
        return created(measure);
    }

    //Update an Existing Measure
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Measure>> updateMeasure(@PathVariable Long id, @Valid @RequestBody MeasureDTO dto) {
        logger.info("Updating measure with id: {}", id);
        Measure updated = service.update(id, dto);
        return success(updated, "Measure updated successfully");
    }

    //Delete/remove a Measure
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMeasure(@PathVariable Long id){
        logger.info("Deleting measure with id: {}", id);
        service.delete(id);
        return success(null, "Measure deleted successfully");
    }
}
