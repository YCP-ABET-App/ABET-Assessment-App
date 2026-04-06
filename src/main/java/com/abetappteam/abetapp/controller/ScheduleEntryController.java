package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.entity.ScheduleEntry;
import com.abetappteam.abetapp.entity.Requests.ScheduleEntry.ScheduleEntrySearchRequest;
import com.abetappteam.abetapp.service.ScheduleEntryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for ScheduleEntry junction operations
 * Manages section ↔ user relationships
 */
@RestController
@RequestMapping("/api/schedule-entry")
public class ScheduleEntryController extends BaseController {

    @Autowired
    private ScheduleEntryService scheduleEntryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduleEntry>>> searchScheduleEntry(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) Integer semesterId,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer programId,
            @RequestParam(required = false) Integer indicatorId
    ) {
        ScheduleEntrySearchRequest request = new ScheduleEntrySearchRequest(id, semesterId, courseId, programId, indicatorId);

        logger.info("Fetching ScheduleEntry with request: {}", request);

        List<ScheduleEntry> results = scheduleEntryService.searchScheduleEntry(request);

        return success(results, "ScheduleEntry results retrieved successfully");
    }


    @PostMapping
    public ResponseEntity<?> createScheduleEntry(@RequestBody ScheduleEntry body) {

        logger.info("Creating ScheduleEntry mapping: {}", body);

        ScheduleEntry created = scheduleEntryService.createScheduleEntry(
                body.getSemesterId(),
                body.getCourseId(),
                body.getProgramId(),
                body.getIndicatorId()
        );

        return success(created, "SectionUser created successfully");
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteScheduleEntry(@PathVariable Long id) {

        logger.info("Deleting ScheduleEntry with id: {}", id);

        validateId(id);
        scheduleEntryService.removeScheduleEntry(id);

        return success(null, "ScheduleEntry deleted successfully");
    }
}

