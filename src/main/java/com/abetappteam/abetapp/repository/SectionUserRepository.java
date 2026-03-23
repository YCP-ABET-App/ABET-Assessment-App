package com.abetappteam.abetapp.repository;

import com.abetappteam.abetapp.entity.SectionUser;
import com.abetappteam.abetapp.entity.Requests.SectionUser.SectionUserSearchRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionUserRepository extends JpaRepository<SectionUser, Long>
{
    // Section ID queries

    List<SectionUser> findBySectionId(int sectionId);

    long countBySectionId(int sectionId);

    // User ID queries

    List<SectionUser> findByUserId(int userId);

    long countByUserId(int userId);

    // Search 

    @Query("SELECT su FROM SectionUser su " +
           "WHERE (:#{#request.id()} IS NULL OR su.id = :#{#request.id()}) " +
           "AND (:#{#request.sectionId()} IS NULL OR su.sectionId = :#{#request.sectionId()}) " +
           "AND (:#{#request.userId()} IS NULL OR su.userId = :#{#request.userId()})")
    List<SectionUser> searchSectionUser(@Param("request") SectionUserSearchRequest request);

    // Exists 

    @Query("SELECT COUNT(su) > 0 FROM SectionUser su " +
           "WHERE su.sectionId = :sectionId AND su.userId = :userId")
    boolean existsBySectionIdAndUserId(@Param("sectionId") int sectionId,
                                       @Param("userId") int userId);
}

