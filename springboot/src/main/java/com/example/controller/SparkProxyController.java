package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.service.SparkApiService;
import com.example.service.SparkTtsService;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/spark")
public class SparkProxyController {

    private static final Logger log = LoggerFactory.getLogger(SparkProxyController.class);

    @Resource
    private SparkApiService sparkApiService;
    @Resource
    private SparkTtsService sparkTtsService;

    @PostMapping("/chat")
    public Result chat(@RequestBody Map<String, Object> requestBody) {
        try {
            Account currentUser = TokenUtils.getCurrentUser();
            if (currentUser == null || !RoleEnum.USER.name().equals(currentUser.getRole())) {
                return Result.error("403", "只有求职者可以使用 AI 面试服务");
            }
            if (requestBody == null) {
                return Result.error("400", "请求体不能为空");
            }
            @SuppressWarnings("unchecked")
            List<Map<String, String>> rawMessages = (List<Map<String, String>>) requestBody.get("messages");

            if (rawMessages == null || rawMessages.isEmpty() || rawMessages.size() > 24) {
                return Result.error("400", "messages数量必须在1到24之间");
            }

            // 转换为 SparkApiService.Message 列表
            List<SparkApiService.Message> messages = new ArrayList<>();
            for (Map<String, String> raw : rawMessages) {
                String role = raw.get("role");
                String content = raw.get("content");
                if (role == null || content == null || content.trim().isEmpty() || content.length() > 12000) {
                    return Result.error("400", "消息格式或长度不合法");
                }
                messages.add(SparkApiService.Message.builder()
                        .role(role)
                        .content(content)
                        .build());
            }

            log.info("Spark proxy: sending {} messages to Spark API", messages.size());
            String reply = sparkApiService.getSparkResponse(String.valueOf(currentUser.getId()), messages);
            log.info("Spark proxy: received reply (length={})", reply != null ? reply.length() : 0);

            return Result.success(reply);

        } catch (Exception e) {
            log.error("Spark proxy error", e);
            return Result.error("502", "AI服务暂时不可用，请稍后重试");
        }
    }

    @PostMapping("/tts")
    public ResponseEntity<byte[]> tts(@RequestBody Map<String, String> requestBody) {
        try {
            Account currentUser = TokenUtils.getCurrentUser();
            if (currentUser == null || !RoleEnum.USER.name().equals(currentUser.getRole())) {
                return ResponseEntity.status(403).build();
            }
            String text = requestBody == null ? null : requestBody.get("text");
            if (text == null || text.trim().isEmpty() || text.length() > 3000) {
                return ResponseEntity.badRequest().build();
            }
            byte[] audio = sparkTtsService.synthesize(text.trim());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .body(audio);
        } catch (Exception e) {
            log.error("Spark TTS proxy error", e);
            return ResponseEntity.status(502).build();
        }
    }
}
