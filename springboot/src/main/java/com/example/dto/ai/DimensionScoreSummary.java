package com.example.dto.ai;

import java.math.BigDecimal;

public class DimensionScoreSummary {

    private String name;
    private BigDecimal averageScore;

    public DimensionScoreSummary() {
    }

    public DimensionScoreSummary(String name, BigDecimal averageScore) {
        this.name = name;
        this.averageScore = averageScore;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(BigDecimal averageScore) {
        this.averageScore = averageScore;
    }
}
