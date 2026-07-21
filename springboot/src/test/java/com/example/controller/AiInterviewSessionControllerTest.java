package com.example.controller;

import com.example.common.Result;
import com.example.dto.ai.InterviewActionResult;
import com.example.service.AiInterviewSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiInterviewSessionControllerTest {

    @Test
    void nextDelegatesToSessionService() {
        AiInterviewSessionService interviewService = mock(AiInterviewSessionService.class);
        InterviewActionResult action = new InterviewActionResult("session-1", "RUNNING", 2, "请继续说明项目难点。");
        when(interviewService.next("session-1")).thenReturn(action);

        AiInterviewSessionController controller = new AiInterviewSessionController();
        ReflectionTestUtils.setField(controller, "interviewService", interviewService);

        Result result = controller.next("session-1");

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isSameAs(action);
        verify(interviewService).next("session-1");
    }

    @Test
    void cancelDelegatesToSessionService() {
        AiInterviewSessionService interviewService = mock(AiInterviewSessionService.class);
        InterviewActionResult action = new InterviewActionResult("session-2", "CANCELLED", 0, null);
        when(interviewService.cancel("session-2")).thenReturn(action);

        AiInterviewSessionController controller = new AiInterviewSessionController();
        ReflectionTestUtils.setField(controller, "interviewService", interviewService);

        Result result = controller.cancel("session-2");

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isSameAs(action);
        verify(interviewService).cancel("session-2");
    }
}
