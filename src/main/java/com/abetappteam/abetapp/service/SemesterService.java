package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.dto.SemesterDTO;
import com.abetappteam.abetapp.entity.Requests.Semester.SemesterSearchRequest;
import com.abetappteam.abetapp.entity.Semester;
import com.abetappteam.abetapp.entity.Semester.SemesterStatus;
import com.abetappteam.abetapp.entity.Semester.SemesterType;
import com.abetappteam.abetapp.exception.BusinessException;
import com.abetappteam.abetapp.exception.ConflictException;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.repository.SemesterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Semester entity
 */
@Service
public class SemesterService extends BaseService<Semester, Long, SemesterRepository> {

    @Autowired
    public SemesterService(SemesterRepository repository) {
        super(repository);
    }

    @Override
    protected String getEntityName() {
        return "Semester";
    }

    @Transactional
    public List<Semester> getAllSemesters(SemesterSearchRequest request) {
        logger.info("Searching semesters with request: {}", request);
        return repository.searchSemesters(
                request.id(),
                request.status(),
                request.academicYear(),
                request.startDate(),
                request.endDate(),
                request.type(),
                request.name(),
                request.code()
        );
    }

    @Transactional
    public Semester createSemester(String name, String code, LocalDate startDate, LocalDate endDate,
                                   Integer academicYear, SemesterType type, Long programId,
                                   String description, Boolean isCurrent) {
        // Check for duplicate semester code in the same program
        List<Semester> existingSemester = repository.searchSemesters(
                -1,
                null,
                -1,
                null,
                null,
                null,
                null,
                code
        );
        if (!existingSemester.isEmpty()) {
            throw new ConflictException("Semester with code '" + code + "' already exists in this program");
        }

        // Validate date range
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("End date cannot be before start date");
        }

        // Validate academic year
        if (academicYear < 2000 || academicYear > 2100) {
            throw new BusinessException("Academic year must be between 2000 and 2100");
        }

        Semester semester = new Semester();
        semester.setName(name);
        semester.setCode(code);
        semester.setStartDate(startDate);
        semester.setEndDate(endDate);
        semester.setAcademicYear(academicYear);
        semester.setType(type);
        semester.setProgramId(programId);
        semester.setDescription(description);
        semester.setStatus(SemesterStatus.UPCOMING);

        // Handle current semester flag
        if (isCurrent != null && isCurrent) {
            setAsCurrentSemester(semester);
        } else {
            semester.setIsCurrent(false);
        }

        logger.info("Creating new semester: {} - {} - {}", code, name, academicYear);
        return repository.save(semester);
    }

    @Transactional
    public Semester createSemester(SemesterDTO dto) {
        SemesterType type = SemesterType.valueOf(dto.getType().toUpperCase());
        return createSemester(dto.getName(), dto.getCode(), dto.getStartDate(), dto.getEndDate(),
                dto.getAcademicYear(), type, dto.getProgramId(), dto.getDescription(),
                dto.getIsCurrent());
    }

    @Transactional
    public Semester updateSemester(Long semesterId, String name, String code, LocalDate startDate,
                                   LocalDate endDate, Integer academicYear, SemesterType type,
                                   String description, Boolean isCurrent) {
        Semester semester = findById(semesterId);

        // Check if semester is editable
        if (!semester.isEditable()) {
            throw new BusinessException("Cannot edit semester that is completed or archived");
        }

        // Check for duplicate semester code
        if (code != null && !code.equals(semester.getCode())) {
            List<Semester> found = repository.searchSemesters(
                    -1,
                    null,
                    -1,
                    null,
                    null,
                    null,
                    null,
                    code
            );

            if(!found.isEmpty()) {
                throw new ConflictException("Semester with code '" + code + "' already exists in this program");
            }

            semester.setCode(code);
        }

        // Validate date range if dates are being updated
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException("End date cannot be before start date");
        }

        if (name != null) {
            semester.setName(name);
        }
        if (startDate != null) {
            semester.setStartDate(startDate);
        }
        if (endDate != null) {
            semester.setEndDate(endDate);
        }
        if (academicYear != null) {
            semester.setAcademicYear(academicYear);
        }
        if (type != null) {
            semester.setType(type);
        }
        if (description != null) {
            semester.setDescription(description);
        }

        // Handle current semester flag
        if (isCurrent != null) {
            if (isCurrent) {
                setAsCurrentSemester(semester);
            } else {
                semester.setIsCurrent(false);
            }
        }

        // Update status based on dates
        updateSemesterStatus(semester);

        logger.info("Updating semester: {}", semesterId);
        return repository.save(semester);
    }

    @Transactional
    public Semester updateSemester(Long semesterId, SemesterDTO dto) {
        SemesterType type = dto.getType() != null ? SemesterType.valueOf(dto.getType().toUpperCase()) : null;
        return updateSemester(semesterId, dto.getName(), dto.getCode(), dto.getStartDate(),
                dto.getEndDate(), dto.getAcademicYear(), type, dto.getDescription(),
                dto.getIsCurrent());
    }

    // TODO: Add checks to prevent deletion if the semester has associated measures or sections
    @Transactional
    public void removeSemester(Long semesterId) {
        Semester semester = findById(semesterId);

        logger.info("Removing semester: {} - {}", semester.getCode(), semester.getName());
        repository.delete(semester);
    }

    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return repository.existsByCodeIgnoreCase(code);
    }


    @Transactional(readOnly = true)
    public long countByProgramAndStatus(Long programId, SemesterStatus status) {
        return repository.countByProgramIdAndStatus(programId, status);
    }

    @Transactional
    public Semester updateSemesterStatus(Long semesterId, SemesterStatus newStatus) {
        Semester semester = findById(semesterId);

        // Validate status transition
        validateStatusTransition(semester.getStatus(), newStatus);

        semester.setStatus(newStatus);
        logger.info("Updating semester {} status to: {}", semesterId, newStatus);
        return repository.save(semester);
    }

    @Transactional
    public Semester setAsCurrentSemester(Long semesterId) {
        Semester semester = findById(semesterId);
        setAsCurrentSemester(semester);
        return repository.save(semester);
    }

    @Transactional
    public void clearCurrentSemesterFlag(Long programId) {
        logger.info("Clearing current semester flag for program: {}", programId);
        repository.clearCurrentSemesterFlag(programId);
    }

    // Helper methods for business logic
    private void setAsCurrentSemester(Semester semester) {
        // Clear current flag from all semesters in the program
        repository.clearCurrentSemesterFlag(semester.getProgramId());
        // Set current flag on this semester
        semester.setIsCurrent(true);
    }

    private void updateSemesterStatus(Semester semester) {
        LocalDate today = LocalDate.now();
        if (today.isBefore(semester.getStartDate())) {
            semester.setStatus(SemesterStatus.UPCOMING);
        } else if (today.isAfter(semester.getEndDate())) {
            semester.setStatus(SemesterStatus.COMPLETED);
        } else {
            semester.setStatus(SemesterStatus.ACTIVE);
        }
    }

    private void validateStatusTransition(SemesterStatus currentStatus, SemesterStatus newStatus) {
        // Add any business rules for status transitions
        if (currentStatus == SemesterStatus.ARCHIVED && newStatus != SemesterStatus.ARCHIVED) {
            throw new BusinessException("Cannot change status of archived semester");
        }

        if (currentStatus == SemesterStatus.COMPLETED && newStatus == SemesterStatus.UPCOMING) {
            throw new BusinessException("Cannot revert completed semester to upcoming status");
        }
    }

    /**
     * Automatically update semester statuses based on current date
     */
    @Transactional
    public void updateAllSemesterStatuses() {
        List<Semester> allSemesters = repository.findAll();
        for (Semester semester : allSemesters) {
            if (semester.isEditable()) { // Only update upcoming and active semesters
                updateSemesterStatus(semester);
                repository.save(semester);
            }
        }
        logger.info("Updated statuses for all semesters");
    }
}