package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.entity.Measure;
import com.abetappteam.abetapp.entity.Semester;
import com.abetappteam.abetapp.service.MultiYearReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for date-range summary report generation.
 * The user supplies a start and end date; the report spans all semesters
 * in that window (e.g., a 6-year ABET accreditation cycle).
 */
@RestController
@RequestMapping("/api/reports")
public class MultiYearReportController extends BaseController {

    @Autowired
    private MultiYearReportService multiYearReportService;

    /**
     * Returns all active measures across every semester that overlaps the
     * requested date range for the given program.
     *
     * GET
     * /api/reports/multi-year?programId=1&startDate=2019-01-01&endDate=2025-01-01
     */
    @GetMapping("/multi-year")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMultiYearReport(
            @RequestParam Long programId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        logger.info("Generating multi-year report for program {} from {} to {}", programId, startDate, endDate);
        validateId(programId);

        List<Semester> semesters = multiYearReportService.getSemestersInDateRange(programId, startDate, endDate);
        List<Measure> measures = multiYearReportService.getMeasuresInDateRange(programId, startDate, endDate);

        Map<String, Object> payload = Map.of(
                "programId", programId,
                "startDate", startDate.toString(),
                "endDate", endDate.toString(),
                "semesterCount", semesters.size(),
                "semesters", semesters,
                "measures", measures);

        return success(payload, "Multi-year report generated successfully");
    }
}