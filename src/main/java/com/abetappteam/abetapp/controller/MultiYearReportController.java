package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.dto.report.MultiYearReportData;
import com.abetappteam.abetapp.service.MultiYearReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
     * Returns a list of per-academic-year reports for every semester that overlaps
     */
    @GetMapping("/multi-year")
    public ResponseEntity<ApiResponse<List<MultiYearReportData>>> getMultiYearReport(
            @RequestParam Long programId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        logger.info("Generating multi-year report for program {} from {} to {}", programId, startDate, endDate);
        validateId(programId);

        List<MultiYearReportData> reports = multiYearReportService.buildReportByAcademicYear(programId, startDate,
                endDate);

        return success(reports, "Multi-year report generated successfully");
    }
}
