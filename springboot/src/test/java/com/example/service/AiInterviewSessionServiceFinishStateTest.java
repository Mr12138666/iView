package com.example.service;

import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.AiInterviewMessage;
import com.example.entity.AiInterviewReport;
import com.example.entity.AiInterviewSession;
import com.example.exception.CustomException;
import com.example.mapper.AiInterviewMessageMapper;
import com.example.mapper.AiInterviewReportMapper;
import com.example.mapper.AiInterviewSessionMapper;
import com.example.mapper.ScoringRuleMapper;
import com.example.utils.TokenUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiInterviewSessionServiceFinishStateTest {

    @Test
    void finishMovesRunningSessionThroughEvaluatingBeforeReportGeneration() throws IOException {
        AiInterviewSessionMapper sessionMapper = mock(AiInterviewSessionMapper.class);
        AiInterviewMessageMapper messageMapper = mock(AiInterviewMessageMapper.class);
        AiInterviewReportMapper reportMapper = mock(AiInterviewReportMapper.class);
        SparkApiService sparkApiService = mock(SparkApiService.class);
        AiInterviewSessionService service = service(sessionMapper, messageMapper, reportMapper, sparkApiService);

        AiInterviewSession session = session("session-1", "RUNNING", 4);
        when(sessionMapper.selectById("session-1")).thenReturn(session);
        when(reportMapper.selectBySessionId("session-1")).thenReturn(null);
        when(sessionMapper.updateToEvaluating("session-1", 4)).thenReturn(1);
        when(messageMapper.selectBySessionId("session-1")).thenReturn(List.of(message("assistant", "Question")));
        when(sparkApiService.getSparkResponse(eq("9"), anyList())).thenReturn(reportJson());

        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            AiInterviewReport report = service.finish("session-1");

            assertThat(report.getSessionId()).isEqualTo("session-1");
        }

        InOrder order = inOrder(sessionMapper, sparkApiService, reportMapper);
        order.verify(sessionMapper).updateToEvaluating("session-1", 4);
        order.verify(sparkApiService).getSparkResponse(eq("9"), anyList());
        order.verify(reportMapper).insert(any(AiInterviewReport.class));
        order.verify(sessionMapper).updateToFinished("session-1");
    }

    @Test
    void finishRestoresRunningStatusWhenAiReportGenerationFails() throws IOException {
        AiInterviewSessionMapper sessionMapper = mock(AiInterviewSessionMapper.class);
        AiInterviewMessageMapper messageMapper = mock(AiInterviewMessageMapper.class);
        AiInterviewReportMapper reportMapper = mock(AiInterviewReportMapper.class);
        SparkApiService sparkApiService = mock(SparkApiService.class);
        AiInterviewSessionService service = service(sessionMapper, messageMapper, reportMapper, sparkApiService);

        AiInterviewSession session = session("session-2", "RUNNING", 2);
        when(sessionMapper.selectById("session-2")).thenReturn(session);
        when(reportMapper.selectBySessionId("session-2")).thenReturn(null);
        when(sessionMapper.updateToEvaluating("session-2", 2)).thenReturn(1);
        when(messageMapper.selectBySessionId("session-2")).thenReturn(List.of(message("assistant", "Question")));
        when(sparkApiService.getSparkResponse(eq("9"), anyList())).thenThrow(new IOException("spark timeout"));

        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            assertThatThrownBy(() -> service.finish("session-2"))
                    .isInstanceOf(CustomException.class)
                    .extracting("code")
                    .isEqualTo("502");
        }

        verify(sessionMapper).updateToRunningFromEvaluating("session-2");
        verify(reportMapper, never()).insert(any(AiInterviewReport.class));
    }

    @Test
    void finishReturnsExistingReportWithoutCallingAiAgain() throws IOException {
        AiInterviewSessionMapper sessionMapper = mock(AiInterviewSessionMapper.class);
        AiInterviewMessageMapper messageMapper = mock(AiInterviewMessageMapper.class);
        AiInterviewReportMapper reportMapper = mock(AiInterviewReportMapper.class);
        SparkApiService sparkApiService = mock(SparkApiService.class);
        AiInterviewSessionService service = service(sessionMapper, messageMapper, reportMapper, sparkApiService);

        AiInterviewSession session = session("session-3", "FINISHED", 5);
        AiInterviewReport existing = new AiInterviewReport();
        existing.setSessionId("session-3");
        existing.setRawContent("{\"questionReviews\":[{\"questionId\":2001,\"score\":88}]}");
        when(sessionMapper.selectById("session-3")).thenReturn(session);
        when(reportMapper.selectBySessionId("session-3")).thenReturn(existing);

        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            AiInterviewReport report = service.finish("session-3");

            assertThat(report).isSameAs(existing);
            assertThat(report.getQuestionReviews()).contains("\"questionId\":2001");
        }

        verify(sparkApiService, never()).getSparkResponse(any(), anyList());
        verify(sessionMapper, never()).updateToEvaluating(any(), any());
    }

    @Test
    void finishRejectsInFlightEvaluationWithoutCreatingDuplicateReport() throws IOException {
        AiInterviewSessionMapper sessionMapper = mock(AiInterviewSessionMapper.class);
        AiInterviewReportMapper reportMapper = mock(AiInterviewReportMapper.class);
        SparkApiService sparkApiService = mock(SparkApiService.class);
        AiInterviewSessionService service = service(sessionMapper, mock(AiInterviewMessageMapper.class),
                reportMapper, sparkApiService);

        AiInterviewSession session = session("session-4", "EVALUATING", 6);
        when(sessionMapper.selectById("session-4")).thenReturn(session);
        when(reportMapper.selectBySessionId("session-4")).thenReturn(null);

        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            assertThatThrownBy(() -> service.finish("session-4"))
                    .isInstanceOf(CustomException.class)
                    .extracting("code")
                    .isEqualTo("409");
        }

        verify(sparkApiService, never()).getSparkResponse(any(), anyList());
        verify(reportMapper, never()).insert(any(AiInterviewReport.class));
    }

    private AiInterviewSessionService service(AiInterviewSessionMapper sessionMapper,
                                              AiInterviewMessageMapper messageMapper,
                                              AiInterviewReportMapper reportMapper,
                                              SparkApiService sparkApiService) {
        AiInterviewSessionService service = new AiInterviewSessionService();
        ReflectionTestUtils.setField(service, "sessionMapper", sessionMapper);
        ReflectionTestUtils.setField(service, "messageMapper", messageMapper);
        ReflectionTestUtils.setField(service, "reportMapper", reportMapper);
        ReflectionTestUtils.setField(service, "sparkApiService", sparkApiService);
        ReflectionTestUtils.setField(service, "scoringRuleMapper", mock(ScoringRuleMapper.class));
        return service;
    }

    private AiInterviewSession session(String id, String status, Integer version) {
        AiInterviewSession session = new AiInterviewSession();
        session.setId(id);
        session.setUserId(9);
        session.setJobPosition("Java backend");
        session.setInterviewType("TECHNICAL");
        session.setDifficulty("MEDIUM");
        session.setDurationMinutes(30);
        session.setInteractionMode("TEXT");
        session.setStatus(status);
        session.setCurrentQuestionNo(2);
        session.setVersion(version);
        return session;
    }

    private AiInterviewMessage message(String role, String content) {
        AiInterviewMessage message = new AiInterviewMessage();
        message.setRole(role);
        message.setContent(content);
        message.setModality("TEXT");
        return message;
    }

    private String reportJson() {
        return """
                {
                  "totalScore": 90,
                  "dimensionScores": {"technicalDepth": 90},
                  "strengths": "Clear answer.",
                  "weaknesses": "Needs more metrics.",
                  "suggestions": "Practice quantifying tradeoffs.",
                  "nextTraining": "System design drill.",
                  "questionReviews": []
                }
                """;
    }

    private MockedStatic<TokenUtils> mockUser() {
        Account account = new Account();
        account.setId(9);
        account.setRole(RoleEnum.USER.name());
        MockedStatic<TokenUtils> tokenUtils = mockStatic(TokenUtils.class);
        tokenUtils.when(TokenUtils::getCurrentUser).thenReturn(account);
        return tokenUtils;
    }
}
