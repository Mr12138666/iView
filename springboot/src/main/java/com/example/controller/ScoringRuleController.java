package com.example.controller;

import com.example.common.Result;
import com.example.entity.ScoringRule;
import com.example.service.ScoringRuleService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scoringRule")
public class ScoringRuleController {

    @Resource
    private ScoringRuleService scoringRuleService;

    @PostMapping("/add")
    public Result add(@RequestBody ScoringRule scoringRule) {
        scoringRuleService.add(scoringRule);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody ScoringRule scoringRule) {
        scoringRuleService.updateById(scoringRule);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        scoringRuleService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        scoringRuleService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        return Result.success(scoringRuleService.selectById(id));
    }

    @GetMapping("/selectAll")
    public Result selectAll(ScoringRule scoringRule) {
        return Result.success(scoringRuleService.selectAll(scoringRule));
    }

    @GetMapping("/selectPage")
    public Result selectPage(ScoringRule scoringRule,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<ScoringRule> pageInfo = scoringRuleService.selectPage(scoringRule, pageNum, pageSize);
        return Result.success(pageInfo);
    }
}
