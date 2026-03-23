package com.abetappteam.abetapp.service;

import java.util.List;

import com.abetappteam.abetapp.entity.Requests.MeasureResults.MeasureResultsSearchRequest;
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

   @Transactional
    public List<MeasureResult> searchMeasureResults(MeasureResultsSearchRequest request) {
        return repository.searchMeasureResults(
                request.id(),
                request.measureId(),
                request.sectionId(),
                request.programId()
        );
    }

    @Transactional
    public MeasureResult createFromImport(MeasureResult mr) {
        return repository.save(mr);
    }
}