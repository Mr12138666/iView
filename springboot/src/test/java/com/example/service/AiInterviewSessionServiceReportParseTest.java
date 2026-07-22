package com.example.service;

import com.example.common.enums.RoleEnum;
import com.example.dto.ai.InterviewSessionDetail;
import com.example.entity.Account;
import com.example.entity.AiInterviewReport;
import com.example.entity.AiInterviewSession;
import com.example.mapper.AiInterviewMessageMapper;
import com.example.mapper.AiInterviewReportMapper;
import com.example.mapper.AiInterviewSessionMapper;
import com.example.utils.TokenUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AiInterviewSessionServiceReportParseTest {

    @Test
    void parseReportExtractsQuestionReviewsFromRawJson() {
        AiInterviewSessionService service = new AiInterviewSessionService();
        String rawJson = """
                {
                  "totalScore": 86,
                  "dimensionScores": {"technicalDepth": 88},
                  "strengths": "Clear layered architecture.",
                  "weaknesses": "Boundary conditions were thin.",
                  "suggestions": "Practice explaining error handling.",
                  "nextTraining": "System design follow-up.",
                  "questionReviews": [
                    {
                      "questionId": 2001,
                      "questionTitle": "Spring Boot request lifecycle",
                      "score": 82,
                      "deductionReason": "Missing transaction boundary details.",
                      "coverage": "Covered Controller, Service, Mapper.",
                      "suggestion": "Add exception and logging strategy."
                    }
                  ]
                }
                """;

        AiInterviewReport report = ReflectionTestUtils.invokeMethod(service, "parseReport", "session-1", rawJson);

        Object questionReviews = ReflectionTestUtils.getField(report, "questionReviews");
        assertThat(questionReviews).isInstanceOf(String.class);
        assertThat((String) questionReviews)
                .contains("\"questionId\":2001")
                .contains("Spring Boot request lifecycle")
                .contains("Missing transaction boundary details.");
    }

    @Test
    void detailEnrichesStoredReportQuestionReviewsFromRawContent() {
        AiInterviewSessionMapper sessionMapper = mock(AiInterviewSessionMapper.class);
        AiInterviewMessageMapper messageMapper = mock(AiInterviewMessageMapper.class);
        AiInterviewReportMapper reportMapper = mock(AiInterviewReportMapper.class);
        AiInterviewSessionService service = new AiInterviewSessionService();
        ReflectionTestUtils.setField(service, "sessionMapper", sessionMapper);
        ReflectionTestUtils.setField(service, "messageMapper", messageMapper);
        ReflectionTestUtils.setField(service, "reportMapper", reportMapper);

        AiInterviewSession session = new AiInterviewSession();
        session.setId("session-2");
        session.setUserId(9);
        session.setStatus("FINISHED");
        when(sessionMapper.selectById("session-2")).thenReturn(session);
        when(messageMapper.selectBySessionId("session-2")).thenReturn(List.of());
        AiInterviewReport stored = new AiInterviewReport();
        stored.setSessionId("session-2");
        stored.setRawContent("{\"questionReviews\":[{\"questionId\":2002,\"score\":90}]}");
        when(reportMapper.selectBySessionId("session-2")).thenReturn(stored);

        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            InterviewSessionDetail detail = service.detail("session-2");
            Object questionReviews = ReflectionTestUtils.getField(detail.getReport(), "questionReviews");

            assertThat((String) questionReviews).contains("\"questionId\":2002");
        }
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
