package com.example.dto.ai;

/**
 * 提交面试回答请求。
 */
public class AnswerInterviewRequest {

    private String answer;
    private String modality;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }
}
