package com.example.controller;

import com.example.common.Result;
import com.example.dto.ai.InterviewSessionDetail;
import com.example.dto.ai.ReportTrendItem;
import com.example.entity.AiInterviewReport;
import com.example.service.AiInterviewSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiReportControllerTest {

    @Test
    void generateDelegatesToSessionFinish() {
        AiInterviewSessionService interviewService = mock(AiInterviewSessionService.class);
        AiInterviewReport report = new AiInterviewReport();
        report.setSessionId("session-1");
        when(interviewService.finish("session-1")).thenReturn(report);

        AiReportController controller = new AiReportController();
        ReflectionTestUtils.setField(controller, "interviewService", interviewService);

        Result result = controller.generate("session-1");

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isSameAs(report);
        verify(interviewService).finish("session-1");
    }

    @Test
    void detailReturnsReportFromSessionDetail() {
        AiInterviewSessionService interviewService = mock(AiInterviewSessionService.class);
        AiInterviewReport report = new AiInterviewReport();
        report.setSessionId("session-2");
        InterviewSessionDetail detail = new InterviewSessionDetail();
        detail.setReport(report);
        when(interviewService.detail("session-2")).thenReturn(detail);

        AiReportController controller = new AiReportController();
        ReflectionTestUtils.setField(controller, "interviewService", interviewService);

        Result result = controller.detail("session-2");

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isSameAs(report);
        verify(interviewService).detail("session-2");
    }

    @Test
    void trendDelegatesToSessionTrend() {
        AiInterviewSessionService interviewService = mock(AiInterviewSessionService.class);
        ReportTrendItem item = new ReportTrendItem();
        item.setSessionId("session-3");
        List<ReportTrendItem> trend = List.of(item);
        when(interviewService.trend()).thenReturn(trend);

        AiReportController controller = new AiReportController();
        ReflectionTestUtils.setField(controller, "interviewService", interviewService);

        Result result = controller.trend();

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isSameAs(trend);
        verify(interviewService).trend();
    }
}
