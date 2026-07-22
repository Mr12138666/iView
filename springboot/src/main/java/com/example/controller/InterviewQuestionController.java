package com.example.controller;

import com.example.common.Result;
import com.example.entity.InterviewQuestion;
import com.example.service.InterviewQuestionService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interviewQuestion")
public class InterviewQuestionController {

    @Resource
    private InterviewQuestionService questionService;

    @PostMapping("/add")
    public Result add(@RequestBody InterviewQuestion question) {
        questionService.add(question);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody InterviewQuestion question) {
        questionService.updateById(question);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        questionService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        questionService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        return Result.success(questionService.selectById(id));
    }

    @GetMapping("/selectAll")
    public Result selectAll(InterviewQuestion question) {
        return Result.success(questionService.selectAll(question));
    }

    @GetMapping("/selectPage")
    public Result selectPage(InterviewQuestion question,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<InterviewQuestion> pageInfo = questionService.selectPage(question, pageNum, pageSize);
        return Result.success(pageInfo);
    }
}
