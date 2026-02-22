package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.service.MeasureResultService;
import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.dto.MeasureResultDTO;
import com.abetappteam.abetapp.dto.PagedResponse;
import com.abetappteam.abetapp.entity.MeasureResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/measure-result")
public class MeasureResultController extends BaseController {

    @Autowired
    private MeasureResultService service;

    // Return all MeasureResults
    @GetMapping
    public ResponseEntity<PagedResponse<MeasureResult>> getAllMeasureResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = createPageable(page, size, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
        Page<MeasureResult> measureResults = service.findAll(pageable);
        return pagedSuccess(measureResults);
    }

    // Return a measure result by id
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<MeasureResult>> getMeasureResult(@PathVariable Long id) {
        logger.info("Fetching measure result with id: {}", id);
        MeasureResult measureResult = service.findById(id);
        return success(measureResult, "Measure result found");
    }

    // Return all active measure results by measure id
    @GetMapping("/byMeasure/{measureId}")
    public ResponseEntity<ApiResponse<List<MeasureResult>>> getMeasureResultsByMeasureId(@PathVariable Long measureId) {
        logger.info("Fetching active measure results for measure id: {}", measureId);
        List<MeasureResult> measureResults = service.findAllActiveByMeasureId(measureId);
        return success(measureResults, "Measure results found");
    }

    // Return all inactive measure results by measure id
    @GetMapping("/byMeasure/Inactive/{measureId}")
    public ResponseEntity<ApiResponse<List<MeasureResult>>> getInactiveMeasureResultsByMeasureId(
            @PathVariable Long measureId) {
        logger.info("Fetching inactive measure results for measure id: {}", measureId);
        List<MeasureResult> measureResults = service.findAllInactiveByMeasureId(measureId);
        return success(measureResults, "Measure results found");
    }

    // Return all active measure results by section id
    @GetMapping("/bySection/{sectionId}")
    public ResponseEntity<ApiResponse<List<MeasureResult>>> getMeasureResultsBySectionId(@PathVariable Long sectionId) {
        logger.info("Fetching active measure results for section id: {}", sectionId);
        List<MeasureResult> measureResults = service.findAllActiveBySectionId(sectionId);
        return success(measureResults, "Measure results found");
    }

    // Return all active measure results by program id
    @GetMapping("/byProgram/{programId}")
    public ResponseEntity<ApiResponse<List<MeasureResult>>> getMeasureResultsByProgramId(@PathVariable Long programId) {
        logger.info("Fetching active measure results for program id: {}", programId);
        List<MeasureResult> measureResults = service.findAllActiveByProgramId(programId);
        return success(measureResults, "Measure results found");
    }

    // Return all active measure results by status
    @GetMapping("/byStatus/{status}")
    public ResponseEntity<ApiResponse<List<MeasureResult>>> getMeasureResultsByStatus(@PathVariable String status) {
        logger.info("Fetching active measure results with status: {}", status);
        List<MeasureResult> measureResults = service.findAllActiveByStatus(status);
        return success(measureResults, "Measure results found");
    }

    // Create a new measure result
    @PostMapping
    public ResponseEntity<ApiResponse<MeasureResult>> createMeasureResult(@Valid @RequestBody MeasureResultDTO dto) {
        logger.info("Creating new measure result for measure id: {}", dto.getMeasureId());
        MeasureResult measureResult = service.create(dto);
        return created(measureResult);
    }

    // Update an existing measure result
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MeasureResult>> updateMeasureResult(@PathVariable Long id,
            @Valid @RequestBody MeasureResultDTO dto) {
        logger.info("Updating measure result with id: {}", id);
        MeasureResult updated = service.update(id, dto);
        return success(updated, "Measure result updated successfully");
    }

    // Delete/remove a measure result
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMeasureResult(@PathVariable Long id) {
        logger.info("Deleting measure result with id: {}", id);
        service.delete(id);
        return success(null, "Measure result deleted successfully");
    }
}