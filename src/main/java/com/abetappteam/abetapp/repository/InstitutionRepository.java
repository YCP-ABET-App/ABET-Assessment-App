package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    List<Institution> findByNameContainingIgnoreCase(String nameFragment);

    Optional<Institution> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}