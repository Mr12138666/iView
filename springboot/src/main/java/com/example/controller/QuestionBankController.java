package com.example.controller;

import com.example.common.Result;
import com.example.entity.QuestionBank;
import com.example.service.QuestionBankService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questionBank")
public class QuestionBankController {

    @Resource
    private QuestionBankService questionBankService;

    @PostMapping("/add")
    public Result add(@RequestBody QuestionBank questionBank) {
        questionBankService.add(questionBank);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody QuestionBank questionBank) {
        questionBankService.updateById(questionBank);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        questionBankService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        questionBankService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        return Result.success(questionBankService.selectById(id));
    }

    @GetMapping("/selectAll")
    public Result selectAll(QuestionBank questionBank) {
        return Result.success(questionBankService.selectAll(questionBank));
    }

    @GetMapping("/selectPage")
    public Result selectPage(QuestionBank questionBank,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<QuestionBank> pageInfo = questionBankService.selectPage(questionBank, pageNum, pageSize);
        return Result.success(pageInfo);
    }
}
