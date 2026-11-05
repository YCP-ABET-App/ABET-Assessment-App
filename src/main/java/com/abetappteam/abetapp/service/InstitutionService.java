package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.dto.InstitutionDTO;
import com.abetappteam.abetapp.entity.Institution;
import com.abetappteam.abetapp.exception.ConflictException;
import com.abetappteam.abetapp.repository.InstitutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InstitutionService extends BaseService<Institution, Long, InstitutionRepository> {

    @Autowired
    public InstitutionService(InstitutionRepository repository) {
        super(repository);
    }

    @Override
    protected String getEntityName() {
        return "Institution";
    }

    @Transactional
    public Institution create(InstitutionDTO dto) {
        if (repository.existsByNameIgnoreCase(dto.getName())) {
            throw new ConflictException("Institution with name '" + dto.getName() + "' already exists");
        }

        Institution institution = new Institution();
        institution.setName(dto.getName());
        institution.setColor(dto.getColor());
        institution.setLogo(dto.getLogo());

        logger.info("Creating new institution: {}", dto.getName());
        return repository.save(institution);
    }

    @Transactional
    public Institution update(Long id, InstitutionDTO dto) {
        Institution institution = findById(id);

        repository.findByNameIgnoreCase(dto.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ConflictException("Institution with name '" + dto.getName() + "' already exists");
            }
        });

        institution.setName(dto.getName());
        institution.setColor(dto.getColor());
        institution.setLogo(dto.getLogo());

        logger.info("Updating institution: {}", id);
        return repository.save(institution);
    }

    @Transactional(readOnly = true)
    public List<Institution> searchByNameFragment(String searchTerm) {
        return repository.findByNameContainingIgnoreCase(searchTerm);
    }
}
