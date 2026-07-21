package com.example.service;

import com.example.common.enums.RoleEnum;
import com.example.dto.ai.ReportTrendItem;
import com.example.entity.Account;
import com.example.entity.AiInterviewReport;
import com.example.entity.AiInterviewSession;
import com.example.mapper.AiInterviewReportMapper;
import com.example.mapper.AiInterviewSessionMapper;
import com.example.utils.TokenUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AiInterviewSessionServiceTrendTest {

    @Test
    void trendReturnsRecentFinishedReportsInChronologicalOrder() {
        AiInterviewSessionMapper sessionMapper = mock(AiInterviewSessionMapper.class);
        AiInterviewReportMapper reportMapper = mock(AiInterviewReportMapper.class);
        AiInterviewSessionService service = new AiInterviewSessionService();
        ReflectionTestUtils.setField(service, "sessionMapper", sessionMapper);
        ReflectionTestUtils.setField(service, "reportMapper", reportMapper);

        AiInterviewSession newer = session("newer", "Java 后端", "TECHNICAL", new Date(2000));
        AiInterviewSession older = session("older", "产品经理", "HR", new Date(1000));
        when(sessionMapper.selectByUserId(7, "FINISHED")).thenReturn(List.of(newer, older));
        when(reportMapper.selectBySessionId("newer")).thenReturn(report("newer", "88.5", "{\"技术深度\":88}"));
        when(reportMapper.selectBySessionId("older")).thenReturn(report("older", "72", "{\"表达能力\":72}"));

        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            List<ReportTrendItem> trend = service.trend();

            assertThat(trend).hasSize(2);
            assertThat(trend.get(0).getSessionId()).isEqualTo("older");
            assertThat(trend.get(0).getJobPosition()).isEqualTo("产品经理");
            assertThat(trend.get(0).getTotalScore()).isEqualByComparingTo("72");
            assertThat(trend.get(1).getSessionId()).isEqualTo("newer");
            assertThat(trend.get(1).getDimensionScores()).isEqualTo("{\"技术深度\":88}");
        }
    }

    private AiInterviewSession session(String id, String jobPosition, String interviewType, Date finishedAt) {
        AiInterviewSession session = new AiInterviewSession();
        session.setId(id);
        session.setJobPosition(jobPosition);
        session.setInterviewType(interviewType);
        session.setDifficulty("MEDIUM");
        session.setStatus("FINISHED");
        session.setFinishedAt(finishedAt);
        return session;
    }

    private AiInterviewReport report(String sessionId, String totalScore, String dimensionScores) {
        AiInterviewReport report = new AiInterviewReport();
        report.setSessionId(sessionId);
        report.setTotalScore(new BigDecimal(totalScore));
        report.setDimensionScores(dimensionScores);
        report.setStrengths("优势");
        report.setSuggestions("建议");
        return report;
    }

    private MockedStatic<TokenUtils> mockUser() {
        Account account = new Account();
        account.setId(7);
        account.setRole(RoleEnum.USER.name());
        MockedStatic<TokenUtils> tokenUtils = mockStatic(TokenUtils.class);
        tokenUtils.when(TokenUtils::getCurrentUser).thenReturn(account);
        return tokenUtils;
    }
}
