package com.example.dto.ai;

import com.example.entity.AiInterviewMessage;
import com.example.entity.AiInterviewReport;
import com.example.entity.AiInterviewSession;

import java.util.List;

/**
 * 面试会话详情响应。
 */
public class InterviewSessionDetail {

    private AiInterviewSession session;
    private List<AiInterviewMessage> messages;
    private AiInterviewReport report;

    public AiInterviewSession getSession() {
        return session;
    }

    public void setSession(AiInterviewSession session) {
        this.session = session;
    }

    public List<AiInterviewMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<AiInterviewMessage> messages) {
        this.messages = messages;
    }

    public AiInterviewReport getReport() {
        return report;
    }

    public void setReport(AiInterviewReport report) {
        this.report = report;
    }
}
