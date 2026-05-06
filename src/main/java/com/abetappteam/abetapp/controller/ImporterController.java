package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.dto.importer.SummaryImportDTO;
import com.abetappteam.abetapp.service.ImporterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/import")
public class ImporterController extends BaseController {

    @Autowired
    private ImporterService importer;

    @PostMapping("/summary")
    public ResponseEntity<ApiResponse<String>> importSummary(@RequestBody SummaryImportDTO dto, @RequestParam Long programId) {
        importer.importSummary(dto, programId);
        return success("Summary imported successfully", "OK");
    }
}
