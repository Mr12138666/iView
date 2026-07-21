package com.example.controller;

import com.example.common.Result;
import com.example.dto.ai.InterviewSessionDetail;
import com.example.service.AiInterviewSessionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI interview report facade.
 */
@RestController
@RequestMapping("/api/report")
public class AiReportController {

    @Resource
    private AiInterviewSessionService interviewService;

    @PostMapping("/generate/{sessionId}")
    public Result generate(@PathVariable String sessionId) {
        return Result.success(interviewService.finish(sessionId));
    }

    @GetMapping("/{sessionId}")
    public Result detail(@PathVariable String sessionId) {
        InterviewSessionDetail detail = interviewService.detail(sessionId);
        return Result.success(detail == null ? null : detail.getReport());
    }

    @GetMapping("/trend")
    public Result trend() {
        return Result.success(interviewService.trend());
    }

    @GetMapping("/summary")
    public Result summary() {
        return Result.success(interviewService.summary());
    }
}
