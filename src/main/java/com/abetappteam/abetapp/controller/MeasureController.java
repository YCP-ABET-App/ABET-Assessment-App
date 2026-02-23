package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.entity.Requests.Measure.MeasureSearchRequest;
import com.abetappteam.abetapp.service.MeasureService;
import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.dto.MeasureDTO;
import com.abetappteam.abetapp.dto.PagedResponse;
import com.abetappteam.abetapp.entity.Measure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<ApiResponse<List<Measure>>> getAllMeasures(@RequestBody MeasureSearchRequest request) {
        logger.info("Fetching all measures");
        List<Measure> measures = service.searchMeasures(request);
        return success(measures, "Measures retrieved successfully");
    }

    //Create a new measure
    @PostMapping
    public ResponseEntity<ApiResponse<Measure>> createMeasure(@Valid @RequestBody MeasureDTO dto) {
        logger.info("Creating new measure: ", dto.getId());
        Measure measure = service.create(dto);
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
