package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.entity.SectionProgram;
import com.abetappteam.abetapp.entity.Requests.SectionProgram.SectionProgramSearchRequest;
import com.abetappteam.abetapp.repository.SectionProgramRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for SectionProgram entity
 */
@Service
public class SectionProgramService extends BaseService<SectionProgram, Long, SectionProgramRepository>
{
    @Autowired
    public SectionProgramService(SectionProgramRepository repository){ super(repository); }

    @Override
    protected String getEntityName() { return "SectionProgram"; }

    @Transactional
    public SectionProgram createSectionProgram(int sectionId, int programId)
    {
        if(repository.existsBySectionIdAndProgramId(sectionId, programId))
        {
            throw new IllegalArgumentException(
                "This section is already assigned to the given program."
            );
        }

        SectionProgram sectionProgram = new SectionProgram();
        sectionProgram.setSectionId(sectionId);
        sectionProgram.setProgramId(programId);

        logger.info("Creating new SectionProgram mapping: {}", sectionProgram);
        return repository.save(sectionProgram);
    }

    @Transactional
    public void removeSectionProgram(Long id)
    {
        SectionProgram sectionProgram = findById(id);
        logger.info("Removing SectionProgram mapping with id {}: {}", id, sectionProgram);
        repository.delete(sectionProgram);
    }

    @Transactional(readOnly = true)
    public List<SectionProgram> searchSectionProgram(SectionProgramSearchRequest request)
    {
        logger.info("Searching SectionProgram with criteria: {}", request);
        return repository.searchSectionProgram(request);
    }
}

