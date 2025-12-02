package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.dto.importer.SummaryImportDTO;
import com.abetappteam.abetapp.service.ImporterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/import")
public class ImporterController extends BaseController {

    @Autowired
    private ImporterService importer;

    @PostMapping("/summary")
    public ResponseEntity<ApiResponse<String>> importSummary(@RequestBody SummaryImportDTO dto) {
        importer.importSummary(dto);
        return success("Summary imported successfully", "OK");
    }
}
