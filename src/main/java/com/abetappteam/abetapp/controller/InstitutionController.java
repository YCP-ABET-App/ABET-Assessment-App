package com.abetappteam.abetapp.controller;

import com.abetappteam.abetapp.dto.ApiResponse;
import com.abetappteam.abetapp.dto.InstitutionDTO;
import com.abetappteam.abetapp.dto.PagedResponse;
import com.abetappteam.abetapp.entity.Institution;
import com.abetappteam.abetapp.service.InstitutionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institution")
public class InstitutionController extends BaseController {

    @Autowired
    private InstitutionService institutionService;

    @GetMapping
    public ResponseEntity<PagedResponse<Institution>> getAllInstitutions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = createPageable(page, size, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
        Page<Institution> institutions = institutionService.findAll(pageable);
        return pagedSuccess(institutions);
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<Institution>> getInstitution(@PathVariable Long id) {
        Institution institution = institutionService.findById(id);
        return success(institution, "Institution found");
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Institution>> createInstitution(@Valid @RequestBody InstitutionDTO dto) {
        Institution institution = institutionService.create(dto);
        return created(institution);
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<Institution>> updateInstitution(
            @PathVariable Long id,
            @Valid @RequestBody InstitutionDTO dto) {
        Institution updated = institutionService.update(id, dto);
        return success(updated, "Institution updated successfully");
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<Void>> deleteInstitution(@PathVariable Long id) {
        institutionService.delete(id);
        return success(null, "Institution deleted successfully");
    }
}