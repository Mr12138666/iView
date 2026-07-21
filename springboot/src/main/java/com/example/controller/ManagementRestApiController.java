package com.example.controller;

import com.example.common.Result;
import com.example.entity.InterviewQuestion;
import com.example.entity.QuestionBank;
import com.example.entity.QuestionBankQuestion;
import com.example.entity.ScoringRule;
import com.example.exception.CustomException;
import com.example.service.InterviewQuestionService;
import com.example.service.QuestionBankQuestionService;
import com.example.service.QuestionBankService;
import com.example.service.ScoringRuleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-style aliases for the AI interview management APIs.
 *
 * <p>The legacy manager UI still uses camel-case paths such as /questionBank/selectPage.
 * These endpoints expose the contract described in the product API draft without breaking
 * those existing callers.</p>
 */
@RestController
public class ManagementRestApiController {

    @Resource
    private QuestionBankService questionBankService;
    @Resource
    private InterviewQuestionService questionService;
    @Resource
    private QuestionBankQuestionService relationService;
    @Resource
    private ScoringRuleService scoringRuleService;

    @PostMapping("/api/question-bank")
    public Result createQuestionBank(@RequestBody QuestionBank questionBank) {
        questionBankService.add(questionBank);
        return Result.success();
    }

    @PutMapping("/api/question-bank/{id}")
    public Result updateQuestionBank(@PathVariable Integer id, @RequestBody QuestionBank questionBank) {
        questionBank.setId(id);
        questionBankService.updateById(questionBank);
        return Result.success();
    }

    @GetMapping("/api/question-bank/{id}")
    public Result getQuestionBank(@PathVariable Integer id) {
        return Result.success(questionBankService.selectById(id));
    }

    @GetMapping("/api/question-bank/page")
    public Result pageQuestionBanks(QuestionBank questionBank,
                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(questionBankService.selectPage(questionBank, pageNum, pageSize));
    }

    @DeleteMapping("/api/question-bank/{id}")
    public Result deleteQuestionBank(@PathVariable Integer id) {
        questionBankService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/api/question")
    public Result createQuestion(@RequestBody InterviewQuestion question) {
        questionService.add(question);
        return Result.success();
    }

    @PutMapping("/api/question/{id}")
    public Result updateQuestion(@PathVariable Integer id, @RequestBody InterviewQuestion question) {
        question.setId(id);
        questionService.updateById(question);
        return Result.success();
    }

    @GetMapping("/api/question/{id}")
    public Result getQuestion(@PathVariable Integer id) {
        return Result.success(questionService.selectById(id));
    }

    @GetMapping("/api/question/page")
    public Result pageQuestions(InterviewQuestion question,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(questionService.selectPage(question, pageNum, pageSize));
    }

    @DeleteMapping("/api/question/{id}")
    public Result deleteQuestion(@PathVariable Integer id) {
        questionService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/api/question-bank/{bankId}/questions")
    public Result attachQuestions(@PathVariable Integer bankId, @RequestBody List<Integer> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            throw new CustomException("400", "questionIds不能为空");
        }
        for (Integer questionId : questionIds) {
            QuestionBankQuestion relation = new QuestionBankQuestion();
            relation.setQuestionBankId(bankId);
            relation.setQuestionId(questionId);
            relationService.add(relation);
        }
        return Result.success();
    }

    @PostMapping("/api/scoring-rule")
    public Result createScoringRule(@RequestBody ScoringRule scoringRule) {
        scoringRuleService.add(scoringRule);
        return Result.success();
    }

    @PutMapping("/api/scoring-rule/{id}")
    public Result updateScoringRule(@PathVariable Integer id, @RequestBody ScoringRule scoringRule) {
        scoringRule.setId(id);
        scoringRuleService.updateById(scoringRule);
        return Result.success();
    }

    @GetMapping("/api/scoring-rule/{id}")
    public Result getScoringRule(@PathVariable Integer id) {
        return Result.success(scoringRuleService.selectById(id));
    }

    @GetMapping("/api/scoring-rule/page")
    public Result pageScoringRules(ScoringRule scoringRule,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(scoringRuleService.selectPage(scoringRule, pageNum, pageSize));
    }

    @DeleteMapping("/api/scoring-rule/{id}")
    public Result deleteScoringRule(@PathVariable Integer id) {
        scoringRuleService.deleteById(id);
        return Result.success();
    }
}
