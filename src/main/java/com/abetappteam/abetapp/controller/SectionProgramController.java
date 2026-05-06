package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.entity.Requests.SectionProgram.SectionProgramSearchRequest;
import com.abetappteam.abetapp.entity.SectionProgram;
import com.abetappteam.abetapp.service.SectionProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for SectionProgram junction operations
 * Manages section ↔ program relationships
 */
@RestController
@RequestMapping("/api/section-program")
public class SectionProgramController extends BaseController {

    @Autowired
    private SectionProgramService sectionProgramService;


    @GetMapping
    public ResponseEntity<ApiResponse<List<SectionProgram>>> searchSectionProgram(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) Integer sectionId,
            @RequestParam(required = false) Integer programId
        ) {
        SectionProgramSearchRequest request = new SectionProgramSearchRequest(id, sectionId, programId);

        logger.info("Fetching SectionPrograms with request: {}", request);

        List<SectionProgram> results = sectionProgramService.searchSectionProgram(request);

        return success(results, "SectionProgram results retrieved successfully");
    }


    @PostMapping
    public ResponseEntity<?> createSectionProgram(@RequestBody SectionProgram body) {

        logger.info("Creating SectionProgram mapping: {}", body);

        SectionProgram created = sectionProgramService.createSectionProgram(
                body.getSectionId(),
                body.getProgramId()
        );

        return success(created, "SectionProgram created successfully");
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSectionProgram(@PathVariable Long id) {

        logger.info("Deleting SectionProgram with id: {}", id);

        validateId(id);
        sectionProgramService.removeSectionProgram(id);

        return success(null, "SectionProgram deleted successfully");
    }
}

