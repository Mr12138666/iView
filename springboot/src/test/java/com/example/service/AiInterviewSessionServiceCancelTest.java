package com.example.service;

import com.example.common.enums.RoleEnum;
import com.example.dto.ai.InterviewActionResult;
import com.example.entity.Account;
import com.example.entity.AiInterviewSession;
import com.example.exception.CustomException;
import com.example.mapper.AiInterviewSessionMapper;
import com.example.utils.TokenUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiInterviewSessionServiceCancelTest {

    @Test
    void cancelMovesCreatedSessionToCancelled() {
        AiInterviewSessionMapper sessionMapper = mock(AiInterviewSessionMapper.class);
        AiInterviewSessionService service = new AiInterviewSessionService();
        ReflectionTestUtils.setField(service, "sessionMapper", sessionMapper);

        AiInterviewSession session = session("session-1", "CREATED", 0);
        AiInterviewSession cancelled = session("session-1", "CANCELLED", 1);
        when(sessionMapper.selectById("session-1")).thenReturn(session, cancelled);
        when(sessionMapper.updateToCancelled("session-1", 0)).thenReturn(1);

        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            InterviewActionResult result = service.cancel("session-1");

            assertThat(result.getSessionId()).isEqualTo("session-1");
            assertThat(result.getStatus()).isEqualTo("CANCELLED");
            verify(sessionMapper).updateToCancelled("session-1", 0);
        }
    }

    @Test
    void cancelRejectsFinishedSession() {
        AiInterviewSessionMapper sessionMapper = mock(AiInterviewSessionMapper.class);
        AiInterviewSessionService service = new AiInterviewSessionService();
        ReflectionTestUtils.setField(service, "sessionMapper", sessionMapper);
        when(sessionMapper.selectById("session-2")).thenReturn(session("session-2", "FINISHED", 3));

        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            assertThatThrownBy(() -> service.cancel("session-2"))
                    .isInstanceOf(CustomException.class)
                    .extracting("code")
                    .isEqualTo("409");
        }
    }

    private AiInterviewSession session(String id, String status, Integer version) {
        AiInterviewSession session = new AiInterviewSession();
        session.setId(id);
        session.setUserId(11);
        session.setStatus(status);
        session.setVersion(version);
        session.setCurrentQuestionNo(0);
        return session;
    }

    private MockedStatic<TokenUtils> mockUser() {
        Account account = new Account();
        account.setId(11);
        account.setRole(RoleEnum.USER.name());
        MockedStatic<TokenUtils> tokenUtils = mockStatic(TokenUtils.class);
        tokenUtils.when(TokenUtils::getCurrentUser).thenReturn(account);
        return tokenUtils;
    }
}
