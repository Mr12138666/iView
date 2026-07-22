package com.example.dto.ai;

/**
 * 面试动作响应。
 */
public class InterviewActionResult {

    private String sessionId;
    private String status;
    private Integer currentQuestionNo;
    private String aiMessage;

    public InterviewActionResult() {
    }

    public InterviewActionResult(String sessionId, String status, Integer currentQuestionNo, String aiMessage) {
        this.sessionId = sessionId;
        this.status = status;
        this.currentQuestionNo = currentQuestionNo;
        this.aiMessage = aiMessage;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCurrentQuestionNo() {
        return currentQuestionNo;
    }

    public void setCurrentQuestionNo(Integer currentQuestionNo) {
        this.currentQuestionNo = currentQuestionNo;
    }

    public String getAiMessage() {
        return aiMessage;
    }

    public void setAiMessage(String aiMessage) {
        this.aiMessage = aiMessage;
    }
}
