package com.abetappteam.abetapp.dto.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Outcome with nested indicators and their measures
 */
public class OutcomeReportData implements Serializable {
    private Long outcomeId;
    private Integer outcomeNumber;
    private String description;
    private String overallStatus;
    private List<IndicatorReportData> indicators;
    private List<String> recommendedActions;

    public OutcomeReportData() {
        this.indicators = new ArrayList<>();
        this.recommendedActions = new ArrayList<>();
    }

    public OutcomeReportData(Long outcomeId, Integer outcomeNumber, String description, String overallStatus) {
        this.outcomeId = outcomeId;
        this.outcomeNumber = outcomeNumber;
        this.description = description;
        this.overallStatus = overallStatus;
        this.indicators = new ArrayList<>();
        this.recommendedActions = new ArrayList<>();
    }

    // Getters and Setters
    public Long getOutcomeId() {
        return outcomeId;
    }

    public void setOutcomeId(Long outcomeId) {
        this.outcomeId = outcomeId;
    }

    public Integer getOutcomeNumber() {
        return outcomeNumber;
    }

    public void setOutcomeNumber(Integer outcomeNumber) {
        this.outcomeNumber = outcomeNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public List<IndicatorReportData> getIndicators() {
        return indicators;
    }

    public void setIndicators(List<IndicatorReportData> indicators) {
        this.indicators = indicators;
    }

    public void addIndicator(IndicatorReportData indicator) {
        this.indicators.add(indicator);
    }

    public List<String> getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(List<String> recommendedActions) {
        this.recommendedActions = recommendedActions;
    }

    public void addRecommendedAction(String action) {
        if (!this.recommendedActions.contains(action)) {
            this.recommendedActions.add(action);
        }
    }
}
