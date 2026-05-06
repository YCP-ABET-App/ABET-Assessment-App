package com.abetappteam.abetapp.dto.importer;

import java.util.List;

public class OutcomeImportDTO {
    private int number;
    private String status;
    private List<IndicatorImportDTO> indicators;

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<IndicatorImportDTO> getIndicators() { return indicators; }
    public void setIndicators(List<IndicatorImportDTO> indicators) { this.indicators = indicators; }
}
