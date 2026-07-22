package com.example.controller;

import com.example.entity.InterviewQuestion;
import com.example.entity.QuestionBank;
import com.example.entity.QuestionBankQuestion;
import com.example.entity.ScoringRule;
import com.example.service.InterviewQuestionService;
import com.example.service.QuestionBankQuestionService;
import com.example.service.QuestionBankService;
import com.example.service.ScoringRuleService;
import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ManagementRestApiControllerTest {

    @Test
    void questionBankRestEndpointsDelegateToExistingService() throws Exception {
        QuestionBankService service = mock(QuestionBankService.class);
        ManagementRestApiController controller = new ManagementRestApiController();
        ReflectionTestUtils.setField(controller, "questionBankService", service);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
        QuestionBank bank = questionBank(1001);
        when(service.selectById(1001)).thenReturn(bank);
        when(service.selectPage(any(QuestionBank.class), eq(2), eq(5))).thenReturn(PageInfo.of(List.of(bank)));

        mvc.perform(post("/api/question-bank")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Java bank\",\"interviewType\":\"TECHNICAL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
        mvc.perform(put("/api/question-bank/1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Java bank updated\",\"interviewType\":\"TECHNICAL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
        mvc.perform(get("/api/question-bank/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1001));
        mvc.perform(get("/api/question-bank/page?pageNum=2&pageSize=5&interviewType=TECHNICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].title").value("Java bank"));
        mvc.perform(delete("/api/question-bank/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        ArgumentCaptor<QuestionBank> bankCaptor = ArgumentCaptor.forClass(QuestionBank.class);
        verify(service).add(any(QuestionBank.class));
        verify(service).updateById(bankCaptor.capture());
        assertThat(bankCaptor.getValue().getId()).isEqualTo(1001);
        verify(service).deleteById(1001);
    }

    @Test
    void questionRestEndpointsDelegateToExistingService() throws Exception {
        InterviewQuestionService service = mock(InterviewQuestionService.class);
        ManagementRestApiController controller = new ManagementRestApiController();
        ReflectionTestUtils.setField(controller, "questionService", service);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
        InterviewQuestion question = question(2001);
        when(service.selectById(2001)).thenReturn(question);
        when(service.selectPage(any(InterviewQuestion.class), eq(1), eq(10))).thenReturn(PageInfo.of(List.of(question)));

        mvc.perform(post("/api/question")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Lifecycle\",\"content\":\"Explain flow\",\"interviewType\":\"TECHNICAL\",\"difficulty\":\"MEDIUM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
        mvc.perform(put("/api/question/2001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Lifecycle\",\"content\":\"Explain flow\",\"interviewType\":\"TECHNICAL\",\"difficulty\":\"MEDIUM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
        mvc.perform(get("/api/question/2001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Lifecycle"));
        mvc.perform(get("/api/question/page?pageNum=1&pageSize=10&difficulty=MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].id").value(2001));
        mvc.perform(delete("/api/question/2001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        ArgumentCaptor<InterviewQuestion> questionCaptor = ArgumentCaptor.forClass(InterviewQuestion.class);
        verify(service).updateById(questionCaptor.capture());
        assertThat(questionCaptor.getValue().getId()).isEqualTo(2001);
        verify(service).deleteById(2001);
    }

    @Test
    void scoringRuleRestEndpointsDelegateToExistingService() throws Exception {
        ScoringRuleService service = mock(ScoringRuleService.class);
        ManagementRestApiController controller = new ManagementRestApiController();
        ReflectionTestUtils.setField(controller, "scoringRuleService", service);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
        ScoringRule rule = scoringRule(3001);
        when(service.selectById(3001)).thenReturn(rule);
        when(service.selectPage(any(ScoringRule.class), eq(1), eq(20))).thenReturn(PageInfo.of(List.of(rule)));

        mvc.perform(post("/api/scoring-rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"interviewType\":\"TECHNICAL\",\"dimension\":\"技术深度\",\"weight\":35}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
        mvc.perform(put("/api/scoring-rule/3001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"interviewType\":\"TECHNICAL\",\"dimension\":\"技术深度\",\"weight\":40}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
        mvc.perform(get("/api/scoring-rule/3001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dimension").value("技术深度"));
        mvc.perform(get("/api/scoring-rule/page?pageNum=1&pageSize=20&interviewType=TECHNICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].id").value(3001));
        mvc.perform(delete("/api/scoring-rule/3001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        ArgumentCaptor<ScoringRule> ruleCaptor = ArgumentCaptor.forClass(ScoringRule.class);
        verify(service).updateById(ruleCaptor.capture());
        assertThat(ruleCaptor.getValue().getId()).isEqualTo(3001);
        verify(service).deleteById(3001);
    }

    @Test
    void batchAttachQuestionsCreatesOneRelationForEachQuestionId() throws Exception {
        QuestionBankQuestionService service = mock(QuestionBankQuestionService.class);
        ManagementRestApiController controller = new ManagementRestApiController();
        ReflectionTestUtils.setField(controller, "relationService", service);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(post("/api/question-bank/1001/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[2001,2002]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        ArgumentCaptor<QuestionBankQuestion> relationCaptor = ArgumentCaptor.forClass(QuestionBankQuestion.class);
        verify(service, times(2)).add(relationCaptor.capture());
        assertThat(relationCaptor.getAllValues())
                .extracting(QuestionBankQuestion::getQuestionBankId)
                .containsExactly(1001, 1001);
        assertThat(relationCaptor.getAllValues())
                .extracting(QuestionBankQuestion::getQuestionId)
                .containsExactly(2001, 2002);
    }

    private QuestionBank questionBank(Integer id) {
        QuestionBank bank = new QuestionBank();
        bank.setId(id);
        bank.setTitle("Java bank");
        bank.setInterviewType("TECHNICAL");
        return bank;
    }

    private InterviewQuestion question(Integer id) {
        InterviewQuestion question = new InterviewQuestion();
        question.setId(id);
        question.setTitle("Lifecycle");
        question.setContent("Explain flow");
        question.setInterviewType("TECHNICAL");
        question.setDifficulty("MEDIUM");
        return question;
    }

    private ScoringRule scoringRule(Integer id) {
        ScoringRule rule = new ScoringRule();
        rule.setId(id);
        rule.setInterviewType("TECHNICAL");
        rule.setDimension("技术深度");
        rule.setWeight(BigDecimal.valueOf(35));
        return rule;
    }
}
