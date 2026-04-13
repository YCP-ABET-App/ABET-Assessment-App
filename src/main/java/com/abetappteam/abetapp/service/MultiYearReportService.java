package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.entity.Course;
import com.abetappteam.abetapp.entity.CourseIndicator;
import com.abetappteam.abetapp.entity.Measure;
import com.abetappteam.abetapp.entity.Semester;
import com.abetappteam.abetapp.exception.BusinessException;
import com.abetappteam.abetapp.repository.CourseIndicatorRepository;
import com.abetappteam.abetapp.repository.CourseRepository;
import com.abetappteam.abetapp.repository.MeasureRepository;
import com.abetappteam.abetapp.repository.SemesterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for generating multi-year summary reports.
 */
@Service
public class MultiYearReportService {

    private static final Logger logger = LoggerFactory.getLogger(MultiYearReportService.class);

    private final SemesterRepository semesterRepository;
    private final CourseRepository courseRepository;
    private final CourseIndicatorRepository courseIndicatorRepository;
    private final MeasureRepository measureRepository;

    @Autowired
    public MultiYearReportService(
            SemesterRepository semesterRepository,
            CourseRepository courseRepository,
            CourseIndicatorRepository courseIndicatorRepository,
            MeasureRepository measureRepository) {
        this.semesterRepository = semesterRepository;
        this.courseRepository = courseRepository;
        this.courseIndicatorRepository = courseIndicatorRepository;
        this.measureRepository = measureRepository;
    }

    // Returns the semesters for the given program whose dates fall within
    // startDate, endDate
    @Transactional(readOnly = true)
    public List<Semester> getSemestersInDateRange(Long programId, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("End date cannot be before start date");
        }
        return semesterRepository.searchSemesters(null, null, null, startDate, endDate, null, null, null)
                .stream()
                .filter(s -> programId.equals(s.getProgramId()))
                .toList();
    }

    // Returns all active measures for the given program across the date range
    @Transactional(readOnly = true)
    public List<Measure> getMeasuresInDateRange(Long programId, LocalDate startDate, LocalDate endDate) {
        // Validate date range via getSemestersInDateRange
        List<Semester> semesters = getSemestersInDateRange(programId, startDate, endDate);
        logger.info("Found {} semesters in date range [{}, {}] for program {}",
                semesters.size(), startDate, endDate, programId);

        List<Course> courses = courseRepository.findActiveCoursesByProgramId(programId);
        List<Measure> output = new ArrayList<>();

        for (Semester semester : semesters) {
            for (Course course : courses) {
                List<CourseIndicator> indicators = courseIndicatorRepository.findByCourseIdAndIsActive(course.getId(),
                        true);

                for (CourseIndicator ci : indicators) {
                    output.addAll(measureRepository.searchMeasures(
                            null, ci.getId().intValue(), semester.getId().intValue(), true));
                }
            }
        }

        logger.info("Aggregated {} measures for program {} in date range [{}, {}]",
                output.size(), programId, startDate, endDate);
        return output;
    }
}