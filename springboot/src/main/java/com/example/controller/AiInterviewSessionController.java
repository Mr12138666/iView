package com.example.controller;

import com.example.common.Result;
import com.example.dto.ai.AnswerInterviewRequest;
import com.example.dto.ai.CreateInterviewRequest;
import com.example.dto.ai.InterviewPageRequest;
import com.example.service.AiInterviewSessionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 面试会话接口。
 */
@RestController
@RequestMapping("/api/interview/session")
public class AiInterviewSessionController {

    @Resource
    private AiInterviewSessionService interviewService;

    @PostMapping
    public Result create(@RequestBody CreateInterviewRequest request) {
        return Result.success(interviewService.create(request));
    }

    @PostMapping("/{id}/start")
    public Result start(@PathVariable String id) {
        return Result.success(interviewService.start(id));
    }

    @PostMapping("/{id}/answer")
    public Result answer(@PathVariable String id, @RequestBody AnswerInterviewRequest request) {
        return Result.success(interviewService.answer(id, request));
    }

    @PostMapping("/{id}/next")
    public Result next(@PathVariable String id) {
        return Result.success(interviewService.next(id));
    }

    @PostMapping("/{id}/cancel")
    public Result cancel(@PathVariable String id) {
        return Result.success(interviewService.cancel(id));
    }

    @PostMapping("/{id}/finish")
    public Result finish(@PathVariable String id) {
        return Result.success(interviewService.finish(id));
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable String id) {
        return Result.success(interviewService.detail(id));
    }

    @GetMapping("/page")
    public Result page(InterviewPageRequest request) {
        return Result.success(interviewService.page(request));
    }
}
