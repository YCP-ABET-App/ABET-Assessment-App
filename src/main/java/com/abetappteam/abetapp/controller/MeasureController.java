package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.entity.Requests.Measure.MeasureSearchRequest;
import com.abetappteam.abetapp.service.MeasureService;
import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.dto.MeasureDTO;
import com.abetappteam.abetapp.entity.Measure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.Valid;

import com.abetappteam.abetapp.entity.ScheduleEntry;
import com.abetappteam.abetapp.repository.MeasureRepository;
import com.abetappteam.abetapp.repository.ScheduleEntryRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/measure")
public class MeasureController extends BaseController{
    
    @Autowired
    private MeasureService service;

    @Autowired
    private MeasureRepository measureRepository;

    @Autowired
    private ScheduleEntryRepository scheduleEntryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Measure>>> searchMeasures(
        @RequestParam(required = false) Integer id,
        @RequestParam(required = false) Integer scheduleEntryId,
        @RequestParam(required = false) Boolean active
    ) {
        MeasureSearchRequest request = new MeasureSearchRequest(id, scheduleEntryId, active);
        logger.info("Fetching measures for request: {}", request);
        List<Measure> measures = service.searchMeasures(request);
        return success(measures, "Measures retrieved successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Measure>> getMeasureById(@PathVariable Long id) {
        logger.info("Fetching measure with id: {}", id);
        Measure measure = service.findById(id);
        return success(measure, "Measure retrieved successfully");
    }

    //Create a new measure
    @PostMapping
    public ResponseEntity<ApiResponse<Measure>> createMeasure(@Valid @RequestBody MeasureDTO dto) {
        logger.info("Creating new measure: ", dto.getDescription());
        Measure measure = service.create(dto);

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
        logger.info("Deactivating measure with id: {}", id);
        service.deactivate(id);
        return success(null, "Measure deleted successfully");
    }

    @GetMapping("/debug")
    public ResponseEntity<?> debugMeasures() {

        List<Measure> measures = measureRepository.findAll();

        List<Map<String, Object>> results = new ArrayList<>();

        for (Measure measure : measures) {

            Map<String, Object> row = new HashMap<>();

            row.put("measureId", measure.getId());
            row.put("scheduleEntryId", measure.getScheduleEntryId());
            row.put("description", measure.getDescription());
            row.put("active", measure.getActive());

            ScheduleEntry scheduleEntry = scheduleEntryRepository
                    .findById(measure.getScheduleEntryId())
                    .orElse(null);

            if (scheduleEntry != null) {
                row.put("semesterId", scheduleEntry.getSemesterId());
                row.put("courseId", scheduleEntry.getCourseId());
                row.put("programId", scheduleEntry.getProgramId());
                row.put("indicatorId", scheduleEntry.getIndicatorId());
            } else {
                row.put("scheduleEntryFound", false);
            }

            results.add(row);
        }

        return ResponseEntity.ok(results);
    }
}
