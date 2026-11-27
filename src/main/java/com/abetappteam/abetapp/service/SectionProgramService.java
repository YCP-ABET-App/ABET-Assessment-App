package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.entity.Measure;
import com.abetappteam.abetapp.entity.MeasureResult;
import com.abetappteam.abetapp.entity.ScheduleEntry;
import com.abetappteam.abetapp.entity.Section;
import com.abetappteam.abetapp.entity.SectionProgram;
import com.abetappteam.abetapp.entity.Requests.SectionProgram.SectionProgramSearchRequest;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.repository.MeasureRepository;
import com.abetappteam.abetapp.repository.MeasureResultRepository;
import com.abetappteam.abetapp.repository.ScheduleEntryRepository;
import com.abetappteam.abetapp.repository.SectionProgramRepository;
import com.abetappteam.abetapp.repository.SectionRepository;
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

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private ScheduleEntryRepository scheduleEntryRepository;

    @Autowired
    private MeasureRepository measureRepository;

    @Autowired
    private MeasureResultRepository measureResultRepository;

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

        Section section = sectionRepository.findById((long) sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", (long) sectionId));

        if(!Boolean.TRUE.equals(scheduleEntryRepository.existsBySemesterIdAndCourseIdAndProgramId(
                section.getSemesterId(), section.getCourseId(), programId)))
        {
            throw new IllegalArgumentException(
                "This program has no performance indicators scheduled for this course and semester."
            );
        }

        SectionProgram sectionProgram = new SectionProgram();
        sectionProgram.setSectionId(sectionId);
        sectionProgram.setProgramId(programId);

        logger.info("Creating new SectionProgram mapping: {}", sectionProgram);
        SectionProgram saved = repository.save(sectionProgram);
        createMissingMeasureResults(section, saved);
        return saved;
    }

    private void createMissingMeasureResults(Section section, SectionProgram sectionProgram) {
        List<ScheduleEntry> scheduleEntries = scheduleEntryRepository.findBySemesterIdAndCourseIdAndProgramId(
                section.getSemesterId(), section.getCourseId(), sectionProgram.getProgramId());
        Long sectionProgramId = sectionProgram.getId();
        for (ScheduleEntry scheduleEntry : scheduleEntries) {
            for (Measure measure : measureRepository.findByScheduleEntryId(scheduleEntry.getId())) {
                if (Boolean.FALSE.equals(measure.getActive())) {
                    continue;
                }
                if (measureResultRepository.existsByMeasureIdAndSectionProgramId(measure.getId(), sectionProgramId)) {
                    continue;
                }
                MeasureResult result = new MeasureResult();
                result.setMeasureId(measure.getId());
                result.setSectionProgramId(sectionProgramId);
                result.setStatus("InProgress");
                measureResultRepository.save(result);
            }
        }
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

