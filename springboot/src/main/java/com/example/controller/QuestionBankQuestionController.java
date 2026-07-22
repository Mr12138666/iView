package com.example.controller;

import com.example.common.Result;
import com.example.entity.QuestionBankQuestion;
import com.example.service.QuestionBankQuestionService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questionBankQuestion")
public class QuestionBankQuestionController {

    @Resource
    private QuestionBankQuestionService relationService;

    @PostMapping("/add")
    public Result add(@RequestBody QuestionBankQuestion relation) {
        relationService.add(relation);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody QuestionBankQuestion relation) {
        relationService.updateById(relation);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        relationService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        relationService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        return Result.success(relationService.selectById(id));
    }

    @GetMapping("/selectAll")
    public Result selectAll(QuestionBankQuestion relation) {
        return Result.success(relationService.selectAll(relation));
    }

    @GetMapping("/selectPage")
    public Result selectPage(QuestionBankQuestion relation,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<QuestionBankQuestion> pageInfo = relationService.selectPage(relation, pageNum, pageSize);
        return Result.success(pageInfo);
    }
}
