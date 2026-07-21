package com.example.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 面试结束后生成的评估报告。
 */
public class AiInterviewReport {

    private Long id;
    private String sessionId;
    private BigDecimal totalScore;
    private String dimensionScores;
    private String strengths;
    private String weaknesses;
    private String suggestions;
    private String nextTraining;
    private String questionReviews;
    private String rawContent;
    private String status;
    private Date createdAt;
    private Date updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public String getDimensionScores() {
        return dimensionScores;
    }

    public void setDimensionScores(String dimensionScores) {
        this.dimensionScores = dimensionScores;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(String weaknesses) {
        this.weaknesses = weaknesses;
    }

    public String getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }

    public String getNextTraining() {
        return nextTraining;
    }

    public void setNextTraining(String nextTraining) {
        this.nextTraining = nextTraining;
    }

    public String getQuestionReviews() {
        return questionReviews;
    }

    public void setQuestionReviews(String questionReviews) {
        this.questionReviews = questionReviews;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
