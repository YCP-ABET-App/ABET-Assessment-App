package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.entity.SectionIndicator;
import com.abetappteam.abetapp.entity.Requests.SectionIndicator.SectionIndicatorRequest;
import com.abetappteam.abetapp.repository.SectionIndicatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SectionIndicatorService extends BaseService<SectionIndicator, Long, SectionIndicatorRepository> {

    private static final Logger logger = LoggerFactory.getLogger(SectionIndicatorService.class);

    @Autowired
    public SectionIndicatorService(SectionIndicatorRepository repository) { super(repository); }

    @Override
    protected String getEntityName() { return "SectionIndicator"; }

    @Transactional
    public SectionIndicator createSectionIndicator(int sectionId, int indicatorId)
    {
        SectionIndicator si = new SectionIndicator();
        si.setSectionId(sectionId);
        si.setIndicatorId(indicatorId);

        logger.info("Creating new SectionIndicator mapping: {}", si);
        return repository.save(si);
    }

    @Transactional
    public SectionIndicator updateSectionIndicator(Long id, Integer sectionId, Integer indicatorId)
    {
        SectionIndicator si = findById(id);

        if(sectionId != null) { si.setSectionId(sectionId); }
        if(indicatorId != null) { si.setIndicatorId(indicatorId); }

        logger.info("Updating SectionIndicator with id {}: {}", id, si);
        return repository.save(si);
    }

    @Transactional
    public void removeSectionIndicator(Long id)
    {
        SectionIndicator si = findById(id);
        logger.info("Removing SectionIndicator with id {}: {}", id, si);
        repository.delete(si);
    }

    @Transactional(readOnly = true)
    public List<SectionIndicator> searchSectionIndicators(SectionIndicatorRequest request)
    {
        logger.info("Searching SectionIndicator with criteria: {}", request);
        return repository.searchSectionIndicators(request.ids(), request.sectionIds(), request.indicatorIds());
    }
}