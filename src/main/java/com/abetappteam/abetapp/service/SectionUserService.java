package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.entity.SectionUser;
import com.abetappteam.abetapp.entity.Requests.SectionUser.SectionUserSearchRequest;
import com.abetappteam.abetapp.repository.SectionUserRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SectionUserService extends BaseService<SectionUser, Long, SectionUserRepository>
{
    @Autowired
    public SectionUserService(SectionUserRepository repository) {super(repository);}

    @Override
    protected String getEntityName() {return "SectionUser";}

    @Transactional
    public SectionUser createSectionUser(int sectionId, int userId)
    {
        if(repository.existsBySectionIdAndUserId(sectionId, userId))
        {
            throw new IllegalArgumentException(
                "This user is already assigned to the given section."
            );
        }

        SectionUser sectionUser = new SectionUser();
        sectionUser.setSectionId(sectionId);
        sectionUser.setUserId(userId);

        logger.info("Creating new SectionUser mapping: {}", sectionUser);
        return repository.save(sectionUser);
    }

    @Transactional
    public void removeSectionUser(Long id)
    {
        SectionUser sectionUser = findById(id);
        logger.info("Removing SectionUser mapping with id {}: {}", id, sectionUser);
        repository.delete(sectionUser);
    }

    @Transactional(readOnly = true)
    public List<SectionUser> searchSectionUser(SectionUserSearchRequest request)
    {
        logger.info("Searching SectionUser with criteria: {}", request);
        return repository.searchSectionUser(request);
    }
}
