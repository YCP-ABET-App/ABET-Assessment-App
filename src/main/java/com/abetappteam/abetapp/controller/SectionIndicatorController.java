package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.entity.Requests.SectionIndicator.SectionIndicatorRequest;
import com.abetappteam.abetapp.entity.SectionIndicator;
import com.abetappteam.abetapp.service.SectionIndicatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/section-indicator")
public class SectionIndicatorController extends BaseController {

    @Autowired
    private SectionIndicatorService sectionIndicatorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SectionIndicator>>> searchSectionIndicators(
            @RequestParam(required = false) List<Integer> ids,
            @RequestParam(required = false) List<Integer> sectionIds,
            @RequestParam(required = false) List<Integer> indicatorIds
    ) {
        SectionIndicatorRequest request = new SectionIndicatorRequest(ids, sectionIds, indicatorIds);
        logger.info("Searching section indicators with request: {}", request);
        List<SectionIndicator> results = sectionIndicatorService.searchSectionIndicators(request);
        return success(results, "Section indicators retrieved successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SectionIndicator>> getSectionIndicator(@PathVariable Long id) {
        logger.info("Fetching section indicator with ID: {}", id);
        validateId(id);
        SectionIndicator si = sectionIndicatorService.findById(id);
        return success(si, "Section indicator retrieved successfully");
    }

    @PostMapping
    public ResponseEntity<?> createSectionIndicator(@RequestBody SectionIndicator body) {
        logger.info("Creating section indicator: {}", body);
        SectionIndicator created = sectionIndicatorService.createSectionIndicator(body.getSectionId(), body.getIndicatorId());
        return success(created, "SectionIndicator created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSectionIndicator(@PathVariable Long id, @RequestBody SectionIndicator body) {
        logger.info("Updating section indicator with id {}: {}", id, body);
        validateId(id);
        SectionIndicator updated = sectionIndicatorService.updateSectionIndicator(id, body.getSectionId(), body.getIndicatorId());
        return success(updated, "SectionIndicator updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSectionIndicator(@PathVariable Long id) {
        logger.info("Deleting section indicator with id: {}", id);
        validateId(id);
        sectionIndicatorService.removeSectionIndicator(id);
        return success(null, "SectionIndicator deleted successfully");
    }
}
