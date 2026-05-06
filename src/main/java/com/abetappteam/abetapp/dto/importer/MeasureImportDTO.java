package com.abetappteam.abetapp.dto.importer;

import java.util.List;

public class MeasureImportDTO {
    private String description;
    private double metPercentage;
    private String status;
    private List<String> recommendedActions;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getMetPercentage() { return metPercentage; }
    public void setMetPercentage(double metPercentage) { this.metPercentage = metPercentage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getRecommendedActions() { return recommendedActions; }
    public void setRecommendedActions(List<String> recommendedActions) { this.recommendedActions = recommendedActions; }
}
