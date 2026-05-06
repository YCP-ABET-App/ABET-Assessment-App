package com.abetappteam.abetapp.dto.importer;

import java.util.List;

public class SummaryImportDTO {
    private Long semesterId;
    private List<OutcomeImportDTO> outcomes;

    public Long getSemesterId() { return semesterId; }
    public void setSemesterId(Long semesterId) { this.semesterId = semesterId; }

    public List<OutcomeImportDTO> getOutcomes() { return outcomes; }
    public void setOutcomes(List<OutcomeImportDTO> outcomes) { this.outcomes = outcomes; }
}
