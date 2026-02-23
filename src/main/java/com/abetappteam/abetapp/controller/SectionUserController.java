package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.entity.SectionUser;
import com.abetappteam.abetapp.entity.Requests.SectionUser.SectionUserSearchRequest;
import com.abetappteam.abetapp.service.SectionUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for SectionUser junction operations
 * Manages section ↔ user relationships
 */
@RestController
@RequestMapping("/api/section-user")
public class SectionUserController extends BaseController {

    @Autowired
    private SectionUserService sectionUserService;


    @GetMapping
    public ResponseEntity<ApiResponse<List<SectionUser>>> searchSectionUser(
            @RequestParam SectionUserSearchRequest request) {

        logger.info("Fetching SectionUser with request: {}", request);

        List<SectionUser> results = sectionUserService.searchSectionUser(request);

        return success(results, "SectionUser results retrieved successfully");
    }


    @PostMapping
    public ResponseEntity<?> createSectionUser(@RequestBody SectionUser body) {

        logger.info("Creating SectionUser mapping: {}", body);

        SectionUser created = sectionUserService.createSectionUser(
                body.getSectionId(),
                body.getUserId()
        );

        return success(created, "SectionUser created successfully");
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSectionUser(@PathVariable Long id) {

        logger.info("Deleting SectionUser with id: {}", id);

        validateId(id);
        sectionUserService.removeSectionUser(id);

        return success(null, "SectionUser deleted successfully");
    }
}

