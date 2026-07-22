package com.example.dto.ai;

public class MultimodalResult {

    private String modality;
    private String status;
    private String transcript;
    private String summary;
    private String suggestion;
    private Long recordId;

    public MultimodalResult() {
    }

    public MultimodalResult(String modality, String status, String summary, String suggestion) {
        this.modality = modality;
        this.status = status;
        this.summary = summary;
        this.suggestion = suggestion;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }
}
