package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.entity.Requests.ScheduleEntry.ScheduleEntrySearchRequest;
import com.abetappteam.abetapp.entity.ScheduleEntry;
import com.abetappteam.abetapp.repository.ScheduleEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScheduleEntryService extends BaseService<ScheduleEntry, Long, ScheduleEntryRepository> {

    @Autowired
    public ScheduleEntryService(ScheduleEntryRepository repository) {super(repository);}

    @Override
    protected String getEntityName() {return "ScheduleEntry";}

    @Transactional
    public ScheduleEntry createScheduleEntry(int semesterId, int courseId, int programId, int indicatorId)
    {
        if(repository.existsBySemesterIdAndCourseIdAndProgramIdAndIndicatorId(semesterId, courseId, programId, indicatorId))
        {
            throw new IllegalArgumentException(
                    "This user is already assigned to the given section."
            );
        }

        ScheduleEntry scheduleEntry = new ScheduleEntry();
        scheduleEntry.setSemesterId(semesterId);
        scheduleEntry.setCourseId(courseId);
        scheduleEntry.setProgramId(programId);
        scheduleEntry.setIndicatorId(indicatorId);

        logger.info("Creating new ScheduleEntry mapping: {}", scheduleEntry);
        return repository.save(scheduleEntry);
    }

    @Transactional
    public void removeScheduleEntry(Long id)
    {
        ScheduleEntry scheduleEntry = findById(id);
        logger.info("Removing ScheduleEntry mapping with id {}: {}", id, scheduleEntry);
        repository.delete(scheduleEntry);
    }

    @Transactional(readOnly = true)
    public List<ScheduleEntry> searchScheduleEntry(ScheduleEntrySearchRequest request)
    {
        logger.info("Searching ScheduleEntry with criteria: {}", request);
        return repository.searchScheduleEntry(request);
    }

}
