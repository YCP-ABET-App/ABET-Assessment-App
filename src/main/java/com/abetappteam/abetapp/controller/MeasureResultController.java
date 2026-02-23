package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.service.MeasureResultService;
import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.dto.MeasureResultDTO;
import com.abetappteam.abetapp.dto.PagedResponse;
import com.abetappteam.abetapp.entity.MeasureResult;
import com.abetappteam.abetapp.entity.Requests.MeasureResults.MeasureResultsSearchRequest;
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<MeasureResult>>> getAllMeasureResults(@RequestParam MeasureResultsSearchRequest request) {
        logger.info("Fetching all measure results");
        List<MeasureResult> measureResults = service.searchMeasureResults(request);
        return success(measureResults, "Measure results retrieved successfully");
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