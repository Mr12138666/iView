package com.example.service;

import com.example.common.enums.RoleEnum;
import com.example.dto.ai.InterviewActionResult;
import com.example.entity.Account;
import com.example.entity.AiInterviewMessage;
import com.example.entity.AiInterviewSession;
import com.example.mapper.AiInterviewMessageMapper;
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

class AiInterviewSessionServiceNextTest {

    @Test
    void nextReturnsLatestAssistantMessageForRunningSession() {
        AiInterviewSessionMapper sessionMapper = mock(AiInterviewSessionMapper.class);
        AiInterviewMessageMapper messageMapper = mock(AiInterviewMessageMapper.class);
        AiInterviewSessionService service = new AiInterviewSessionService();
        ReflectionTestUtils.setField(service, "sessionMapper", sessionMapper);
        ReflectionTestUtils.setField(service, "messageMapper", messageMapper);

        AiInterviewSession session = new AiInterviewSession();
        session.setId("session-1");
        session.setUserId(9);
        session.setStatus("RUNNING");
        session.setCurrentQuestionNo(3);
        when(sessionMapper.selectById("session-1")).thenReturn(session);
        when(messageMapper.selectBySessionId("session-1")).thenReturn(List.of(
                message("user", "我的回答"),
                message("assistant", "请继续说明项目难点。")
        ));

        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            InterviewActionResult result = service.next("session-1");

            assertThat(result.getSessionId()).isEqualTo("session-1");
            assertThat(result.getStatus()).isEqualTo("RUNNING");
            assertThat(result.getCurrentQuestionNo()).isEqualTo(3);
            assertThat(result.getAiMessage()).isEqualTo("请继续说明项目难点。");
        }
    }

    private AiInterviewMessage message(String role, String content) {
        AiInterviewMessage message = new AiInterviewMessage();
        message.setRole(role);
        message.setContent(content);
        message.setModality("TEXT");
        return message;
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
