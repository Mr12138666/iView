package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.dto.ai.MultimodalResult;
import com.example.dto.ai.MultimodalTextRequest;
import com.example.entity.Account;
import com.example.entity.MultimodalRecord;
import com.example.service.MultimodalRecordService;
import com.example.service.SparkTtsService;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/multimodal")
public class MultimodalController {

    @Resource
    private SparkTtsService sparkTtsService;
    @Resource
    private MultimodalRecordService recordService;

    @PostMapping("/asr")
    public Result asr(@RequestParam(value = "sessionId", required = false) String sessionId,
                      @RequestParam(value = "file", required = false) MultipartFile file) {
        Account currentUser = requireUser();
        if (file == null || file.isEmpty()) {
            return Result.error("400", "请上传音频文件");
        }
        MultimodalRecord record = recordService.createAudioRecord(
                currentUser, sessionId, file.getOriginalFilename(), file.getSize());
        MultimodalResult result = new MultimodalResult(
                "AUDIO",
                "RESERVED",
                "ASR 语音转写接口已预留，等待接入正式语音识别服务",
                "当前版本可继续使用浏览器语音识别或文本作答"
        );
        result.setRecordId(record.getId());
        result.setTranscript("");
        return Result.success(result);
    }

    @PostMapping("/tts")
    public ResponseEntity<byte[]> tts(@RequestBody MultimodalTextRequest request) {
        requireUser();
        if (request == null || request.getText() == null
                || request.getText().trim().isEmpty() || request.getText().length() > 3000) {
            return ResponseEntity.badRequest().build();
        }
        try {
            byte[] audio = sparkTtsService.synthesize(request.getText().trim());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .body(audio);
        } catch (Exception e) {
            return ResponseEntity.status(502).build();
        }
    }

    @PostMapping("/video-analysis")
    public Result videoAnalysis(@RequestParam(value = "sessionId", required = false) String sessionId,
                                @RequestParam(value = "file", required = false) MultipartFile file) {
        Account currentUser = requireUser();
        if (file == null || file.isEmpty()) {
            return Result.error("400", "请上传视频片段或图像帧");
        }
        MultimodalRecord record = recordService.createVideoRecord(
                currentUser, sessionId, file.getOriginalFilename(), file.getSize());
        MultimodalResult result = new MultimodalResult(
                "VIDEO",
                "RESERVED",
                "视频非语言分析接口已预留",
                "后续可接入表情、眼神、姿态和稳定性分析模型"
        );
        result.setRecordId(record.getId());
        return Result.success(result);
    }

    @PostMapping("/code-run")
    public Result codeRun(@RequestBody MultimodalTextRequest request) {
        Account currentUser = requireUser();
        if (request == null || request.getCode() == null || request.getCode().trim().isEmpty()) {
            return Result.error("400", "代码内容不能为空");
        }
        if (request.getCode().length() > 20000) {
            return Result.error("400", "代码内容不能超过20000个字符");
        }
        MultimodalRecord record = recordService.createCodeRecord(
                currentUser, request.getSessionId(), request.getLanguage(), request.getCode());
        Map<String, Object> payload = Map.of(
                "recordId", record.getId(),
                "modality", "CODE",
                "status", "RESERVED",
                "language", request.getLanguage() == null ? "unknown" : request.getLanguage(),
                "compileOutput", "",
                "runOutput", "",
                "suggestion", "代码运行沙箱接口已预留。当前版本只保存和评估代码文本，不在服务端执行不受信任代码。"
        );
        return Result.success(payload);
    }

    @GetMapping("/session/{sessionId}")
    public Result listBySession(@PathVariable String sessionId) {
        Account currentUser = requireUser();
        return Result.success(recordService.listBySession(currentUser, sessionId));
    }

    private Account requireUser() {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new com.example.exception.CustomException("401", "用户未登录");
        }
        if (!RoleEnum.USER.name().equals(currentUser.getRole())) {
            throw new com.example.exception.CustomException("403", "只有求职者可以使用多模态面试能力");
        }
        return currentUser;
    }
}
