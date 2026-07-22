package com.example.dto.ai;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReportSummary {

    private Integer reportCount;
    private BigDecimal averageScore;
    private BigDecimal highestScore;
    private BigDecimal latestScore;
    private BigDecimal scoreDelta;
    private List<DimensionScoreSummary> weakestDimensions = new ArrayList<>();
    private List<String> trainingSuggestions = new ArrayList<>();
    private List<ReportTrendItem> trend = new ArrayList<>();

    public Integer getReportCount() {
        return reportCount;
    }

    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }

    public BigDecimal getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(BigDecimal averageScore) {
        this.averageScore = averageScore;
    }

    public BigDecimal getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(BigDecimal highestScore) {
        this.highestScore = highestScore;
    }

    public BigDecimal getLatestScore() {
        return latestScore;
    }

    public void setLatestScore(BigDecimal latestScore) {
        this.latestScore = latestScore;
    }

    public BigDecimal getScoreDelta() {
        return scoreDelta;
    }

    public void setScoreDelta(BigDecimal scoreDelta) {
        this.scoreDelta = scoreDelta;
    }

    public List<DimensionScoreSummary> getWeakestDimensions() {
        return weakestDimensions;
    }

    public void setWeakestDimensions(List<DimensionScoreSummary> weakestDimensions) {
        this.weakestDimensions = weakestDimensions;
    }

    public List<String> getTrainingSuggestions() {
        return trainingSuggestions;
    }

    public void setTrainingSuggestions(List<String> trainingSuggestions) {
        this.trainingSuggestions = trainingSuggestions;
    }

    public List<ReportTrendItem> getTrend() {
        return trend;
    }

    public void setTrend(List<ReportTrendItem> trend) {
        this.trend = trend;
    }
}
