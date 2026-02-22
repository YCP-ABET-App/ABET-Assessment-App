package com.abetappteam.abetapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abetappteam.abetapp.entity.MeasureResult;
import com.abetappteam.abetapp.repository.MeasureResultRepository;
import com.abetappteam.abetapp.dto.MeasureResultDTO;

@Service
public class MeasureResultService extends BaseService<MeasureResult, Long, MeasureResultRepository> {

    @Autowired
    public MeasureResultService(MeasureResultRepository repository) {
        super(repository);
    }

    @Override
    protected String getEntityName() {
        return "MeasureResult";
    }

    // Create new MeasureResult
    @Transactional
    public MeasureResult create(MeasureResultDTO dto) {
        MeasureResult measureResult = new MeasureResult();
        measureResult.setMeasureId(dto.getMeasureId());
        measureResult.setSectionId(dto.getSectionId());
        measureResult.setProgramId(dto.getProgramId());
        measureResult.setStudentsMet(dto.getStudentsMet());
        measureResult.setStudentsExceeded(dto.getStudentsExceeded());
        measureResult.setStudentsBelow(dto.getStudentsBelow());
        measureResult.setObservation(dto.getObservation());
        measureResult.setRejectionNote(dto.getRejectionNote());
        measureResult.setStatus(dto.getStatus());

        logger.info("Creating new measure result for measure ID: {}", dto.getMeasureId());
        return repository.save(measureResult);
    }

    // Update existing MeasureResult
    @Transactional
    public MeasureResult update(Long id, MeasureResultDTO dto) {
        MeasureResult measureResult = findById(id);

        measureResult.setMeasureId(dto.getMeasureId());
        measureResult.setSectionId(dto.getSectionId());
        measureResult.setProgramId(dto.getProgramId());
        measureResult.setStudentsMet(dto.getStudentsMet());
        measureResult.setStudentsExceeded(dto.getStudentsExceeded());
        measureResult.setStudentsBelow(dto.getStudentsBelow());
        measureResult.setObservation(dto.getObservation());
        measureResult.setRejectionNote(dto.getRejectionNote());
        measureResult.setStatus(dto.getStatus());

        logger.info("Updating measure result: {}", id);
        return repository.save(measureResult);
    }

    // Return all measure results by status
    @Transactional(readOnly = true)
    public List<MeasureResult> findAllByStatus(String status) {
        logger.debug("Fetching measure results with status: {}", status);
        return repository.findByStatus(status);
    }

    // Return all active MeasureResults for a specific measure
    @Transactional(readOnly = true)
    public List<MeasureResult> findAllActiveByMeasureId(Long measureId) {
        logger.debug("Fetching active measure results for measure ID: {}", measureId);
        return repository.findByMeasureIdAndActiveTrue(measureId);
    }

    // Return all inactive MeasureResults for a specific measure
    @Transactional(readOnly = true)
    public List<MeasureResult> findAllInactiveByMeasureId(Long measureId) {
        logger.debug("Fetching inactive measure results for measure ID: {}", measureId);
        return repository.findByMeasureIdAndActiveFalse(measureId);
    }

    // Return all MeasureResults for a specific measure regardless of status
    @Transactional(readOnly = true)
    public List<MeasureResult> findAllByMeasureId(Long measureId) {
        logger.debug("Fetching all measure results for measure ID: {}", measureId);
        return repository.findByMeasureId(measureId);
    }

    // Return all active MeasureResults for a specific section
    @Transactional(readOnly = true)
    public List<MeasureResult> findAllActiveBySectionId(Long sectionId) {
        logger.debug("Fetching active measure results for section ID: {}", sectionId);
        return repository.findBySectionIdAndActiveTrue(sectionId);
    }

    // Return all active MeasureResults for a specific program
    @Transactional(readOnly = true)
    public List<MeasureResult> findAllActiveByProgramId(Long programId) {
        logger.debug("Fetching active measure results for program ID: {}", programId);
        return repository.findByProgramIdAndActiveTrue(programId);
    }

    // Return all active MeasureResults by status
    @Transactional(readOnly = true)
    public List<MeasureResult> findAllActiveByStatus(String status) {
        logger.debug("Fetching active measure results with status: {}", status);
        return repository.findByActiveTrueAndStatus(status);
    }

    @Transactional
    public MeasureResult createFromImport(MeasureResult mr) {
        return repository.save(mr);
    }
}