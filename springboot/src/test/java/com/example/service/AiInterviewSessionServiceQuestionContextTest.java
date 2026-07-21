package com.example.service;

import com.example.common.enums.RoleEnum;
import com.example.dto.ai.AnswerInterviewRequest;
import com.example.entity.Account;
import com.example.entity.AiInterviewMessage;
import com.example.entity.AiInterviewSession;
import com.example.entity.InterviewQuestion;
import com.example.entity.QuestionBank;
import com.example.entity.QuestionBankQuestion;
import com.example.mapper.AiInterviewMessageMapper;
import com.example.mapper.AiInterviewSessionMapper;
import com.example.mapper.InterviewQuestionMapper;
import com.example.mapper.QuestionBankMapper;
import com.example.mapper.QuestionBankQuestionMapper;
import com.example.utils.TokenUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AiInterviewSessionServiceQuestionContextTest {

    @Test
    void startStoresFirstAssistantMessageWithSelectedQuestionId() throws IOException {
        AiInterviewSessionMapper sessionMapper = mock(AiInterviewSessionMapper.class);
        AiInterviewMessageMapper messageMapper = mock(AiInterviewMessageMapper.class);
        SparkApiService sparkApiService = mock(SparkApiService.class);
        QuestionBankMapper questionBankMapper = mock(QuestionBankMapper.class);
        QuestionBankQuestionMapper relationMapper = mock(QuestionBankQuestionMapper.class);
        InterviewQuestionMapper questionMapper = mock(InterviewQuestionMapper.class);
        AiInterviewSessionService service = service(sessionMapper, messageMapper, sparkApiService,
                questionBankMapper, relationMapper, questionMapper);

        when(sessionMapper.selectById("session-1")).thenReturn(session("session-1", "CREATED", 0));
        when(sessionMapper.updateToRunning("session-1", 0)).thenReturn(1);
        when(questionBankMapper.selectAll(any(QuestionBank.class))).thenReturn(List.of(bank()));
        when(relationMapper.selectAll(any(QuestionBankQuestion.class))).thenReturn(List.of(relation(2001)));
        when(questionMapper.selectById(2001)).thenReturn(question(2001));
        when(sparkApiService.getSparkResponse(eq("9"), anyList())).thenReturn("First AI question");
        ArgumentCaptor<AiInterviewMessage> messageCaptor = ArgumentCaptor.forClass(AiInterviewMessage.class);
        when(messageMapper.insert(messageCaptor.capture())).thenReturn(1);

        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            service.start("session-1");
        }

        assertThat(messageCaptor.getAllValues())
                .filteredOn(message -> "assistant".equals(message.getRole()))
                .singleElement()
                .extracting(AiInterviewMessage::getQuestionId)
                .isEqualTo(2001);
    }

    @Test
    void answerStoresUserMessageWithLatestAssistantQuestionId() throws IOException {
        AiInterviewSessionMapper sessionMapper = mock(AiInterviewSessionMapper.class);
        AiInterviewMessageMapper messageMapper = mock(AiInterviewMessageMapper.class);
        SparkApiService sparkApiService = mock(SparkApiService.class);
        AiInterviewSessionService service = service(sessionMapper, messageMapper, sparkApiService,
                mock(QuestionBankMapper.class), mock(QuestionBankQuestionMapper.class), mock(InterviewQuestionMapper.class));

        when(sessionMapper.selectById("session-2")).thenReturn(session("session-2", "RUNNING", 1));
        when(messageMapper.selectBySessionId("session-2")).thenReturn(List.of(message("assistant", "First question", 2001)));
        when(messageMapper.selectMaxOrder("session-2")).thenReturn(1);
        when(sparkApiService.getSparkResponse(eq("9"), anyList())).thenReturn("Follow-up question");
        ArgumentCaptor<AiInterviewMessage> messageCaptor = ArgumentCaptor.forClass(AiInterviewMessage.class);
        when(messageMapper.insert(messageCaptor.capture())).thenReturn(1);

        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            AnswerInterviewRequest request = new AnswerInterviewRequest();
            request.setAnswer("My answer");
            request.setModality("TEXT");
            service.answer("session-2", request);
        }

        assertThat(messageCaptor.getAllValues())
                .filteredOn(message -> "user".equals(message.getRole()))
                .singleElement()
                .extracting(AiInterviewMessage::getQuestionId)
                .isEqualTo(2001);
    }

    private AiInterviewSessionService service(AiInterviewSessionMapper sessionMapper,
                                              AiInterviewMessageMapper messageMapper,
                                              SparkApiService sparkApiService,
                                              QuestionBankMapper questionBankMapper,
                                              QuestionBankQuestionMapper relationMapper,
                                              InterviewQuestionMapper questionMapper) {
        AiInterviewSessionService service = new AiInterviewSessionService();
        ReflectionTestUtils.setField(service, "sessionMapper", sessionMapper);
        ReflectionTestUtils.setField(service, "messageMapper", messageMapper);
        ReflectionTestUtils.setField(service, "sparkApiService", sparkApiService);
        ReflectionTestUtils.setField(service, "questionBankMapper", questionBankMapper);
        ReflectionTestUtils.setField(service, "questionBankQuestionMapper", relationMapper);
        ReflectionTestUtils.setField(service, "interviewQuestionMapper", questionMapper);
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
        session.setCurrentQuestionNo(1);
        session.setVersion(version);
        return session;
    }

    private QuestionBank bank() {
        QuestionBank bank = new QuestionBank();
        bank.setId(1001);
        bank.setTitle("Java backend bank");
        bank.setJobDirection("Java backend");
        return bank;
    }

    private QuestionBankQuestion relation(Integer questionId) {
        QuestionBankQuestion relation = new QuestionBankQuestion();
        relation.setQuestionBankId(1001);
        relation.setQuestionId(questionId);
        return relation;
    }

    private InterviewQuestion question(Integer id) {
        InterviewQuestion question = new InterviewQuestion();
        question.setId(id);
        question.setTitle("Spring Boot request lifecycle");
        question.setContent("Explain Controller-Service-Mapper flow.");
        question.setEnabled(1);
        return question;
    }

    private AiInterviewMessage message(String role, String content, Integer questionId) {
        AiInterviewMessage message = new AiInterviewMessage();
        message.setRole(role);
        message.setContent(content);
        message.setModality("TEXT");
        message.setQuestionId(questionId);
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
