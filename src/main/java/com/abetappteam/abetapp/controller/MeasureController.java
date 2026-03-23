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

@RestController
@RequestMapping("/api/measure")
public class MeasureController extends BaseController{
    
    @Autowired
    private MeasureService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Measure>>> searchMeasures(
        @RequestParam(required = false) Integer id,
        @RequestParam(required = false) Integer courseIndicatorId,
        @RequestParam(required = false) Integer semesterId,
        @RequestParam(required = false) Boolean active
    ) {
        MeasureSearchRequest request = new MeasureSearchRequest(id, courseIndicatorId, semesterId, active);
        logger.info("Fetching measures for request: {}", request);
        List<Measure> measures = service.searchMeasures(request);
        return success(measures, "Measures retrieved successfully");
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
}
