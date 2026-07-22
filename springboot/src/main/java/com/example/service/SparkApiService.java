package com.example.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.common.config.SparkApiConfig;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SparkApiService {

    private static final Logger log = LoggerFactory.getLogger(SparkApiService.class);
    private final SparkApiConfig sparkApiConfig;
    private final OkHttpClient client;

    // 内部类 Message，用于构建对话历史
    public static class Message {
        private String role;
        private String content;

        // 私有构造函数，强制使用builder
        private Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }

        // Builder模式入口
        public static MessageBuilder builder() {
            return new MessageBuilder();
        }

        public static class MessageBuilder {
            private String role;
            private String content;

            public MessageBuilder role(String role) {
                this.role = role;
                return this;
            }

            public MessageBuilder content(String content) {
                this.content = content;
                return this;
            }

            public Message build() {
                if (this.role == null || this.content == null) {
                    // 或者根据你的业务逻辑，允许content为空（例如，某些特殊role）
                    throw new IllegalArgumentException("Role and Content cannot be null for a Message.");
                }
                // 确保 role 是小写且是有效值之一
                String lowerCaseRole = this.role.toLowerCase();
                if (!("system".equals(lowerCaseRole) || "user".equals(lowerCaseRole) || "assistant".equals(lowerCaseRole) || "tool".equals(lowerCaseRole))) {
                    log.warn("Attempting to build Message with potentially invalid role: '{}'. Standard roles are 'system', 'user', 'assistant', 'tool'.", this.role);
                    // 可以选择抛出异常，或者容错处理（例如，默认转为 "user" 或记录更详细日志）
                    // throw new IllegalArgumentException("Invalid role: " + this.role + ". Must be one of 'system', 'user', 'assistant', 'tool'.");
                }
                // 存储时统一使用小写，确保一致性
                return new Message(lowerCaseRole, this.content);
            }
        }
    }

    public SparkApiService(SparkApiConfig sparkApiConfig) {
        this.sparkApiConfig = sparkApiConfig;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    public String getSparkResponse(String platformUserId, List<Message> messageHistory) throws IOException {
        if (hasWebSocketConfig()) {
            return getSparkResponseByWebSocket(platformUserId, messageHistory);
        }
        if (sparkApiConfig.getApiPassword() == null || sparkApiConfig.getApiPassword().isBlank()) {
            throw new IOException("讯飞星火API未配置");
        }
        return getSparkResponseByHttp(platformUserId, messageHistory);
    }

    private String getSparkResponseByHttp(String platformUserId, List<Message> messageHistory) throws IOException {
        JSONObject requestJson = new JSONObject();
        requestJson.put("model", sparkApiConfig.getModelName());

        JSONArray messagesArray = new JSONArray();
        for (Message msg : messageHistory) {
            JSONObject m = new JSONObject();
            // 直接使用 Message 对象中已经处理过（例如转为小写）的 role
            m.put("role", msg.getRole());
            m.put("content", msg.getContent());
            messagesArray.add(m);
        }
        // **********************************************************************
        // ** 添加DEBUG日志，用于检查实际发送的messages数组内容 **
        // **********************************************************************
        log.debug("Constructed messages array for Spark API: {}", messagesArray.toJSONString());
        requestJson.put("messages", messagesArray);


        requestJson.put("temperature", 0.6);
        requestJson.put("max_tokens", 2048);
        if (platformUserId != null && !platformUserId.isEmpty()) {
            requestJson.put("user", platformUserId); // 对应API文档中的 user 字段
        }

        log.info("Sending request to Spark API (OpenAI compatible). Model: {}, URL: {}",
                sparkApiConfig.getModelName(), sparkApiConfig.getHostUrl());

        RequestBody body = RequestBody.create(requestJson.toJSONString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(sparkApiConfig.getHostUrl())
                .addHeader("Authorization", "Bearer " + sparkApiConfig.getApiPassword())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBodyString = "";
            if (response.body() != null) {
                responseBodyString = response.body().string();
            }
            // log.debug("Spark API Response Body: {}", responseBodyString); // 可以取消注释以查看完整响应

            if (!response.isSuccessful()) {
                log.error("Spark API Request Failed: Status Code - {}, Body - {}", response.code(), responseBodyString);
                if (responseBodyString != null && !responseBodyString.isEmpty()) {
                    try {
                        JSONObject errorObject = JSON.parseObject(responseBodyString);
                        if (errorObject.containsKey("error") && errorObject.getJSONObject("error").containsKey("message")) {
                            throw new IOException("讯飞星火API请求失败: " + errorObject.getJSONObject("error").getString("message") + " (HTTP Status: " + response.code() + ")");
                        }
                    } catch (Exception ignored) {}
                }
                throw new IOException("讯飞星火API请求失败，状态码: " + response.code() + ". 详情: " + responseBodyString);
            }

            JSONObject responseObject = JSON.parseObject(responseBodyString);
            if (responseObject.containsKey("code") && responseObject.getInteger("code") != 0) {
                log.error("Spark API Error (from response body): Code - {}, Message - {}, SID - {}",
                        responseObject.getInteger("code"),
                        responseObject.getString("message"),
                        responseObject.getString("sid"));
                throw new IOException("讯飞星火API返回业务错误: " + responseObject.getString("message") +
                        " (Code: " + responseObject.getInteger("code") +
                        ", SID: " + responseObject.getString("sid") + ")");
            }

            JSONArray choicesArray = responseObject.getJSONArray("choices");
            if (choicesArray != null && !choicesArray.isEmpty()) {
                JSONObject firstChoice = choicesArray.getJSONObject(0);
                if (firstChoice.containsKey("message") && firstChoice.getJSONObject("message").containsKey("content")) {
                    return firstChoice.getJSONObject("message").getString("content");
                }
            }

            log.warn("Spark API did not return expected content structure. Full response: {}", responseBodyString);
            return "AI未能生成有效回复（响应结构不符），请稍后再试。";
        }
    }

    private String getSparkResponseByWebSocket(String platformUserId, List<Message> messageHistory) throws IOException {
        CountDownLatch done = new CountDownLatch(1);
        StringBuilder content = new StringBuilder();
        AtomicReference<String> failure = new AtomicReference<>();

        Request request;
        try {
            request = new Request.Builder().url(createChatAuthUrl()).build();
        } catch (Exception e) {
            throw new IOException("讯飞星火WebSocket鉴权地址生成失败", e);
        }

        WebSocket webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                JSONObject chatRequest = buildChatRequest(platformUserId, messageHistory);
                JSONArray text = chatRequest.getJSONObject("payload").getJSONObject("message").getJSONArray("text");
                log.info("Sending request to Spark WebSocket chat. Domain: {}, messageCount: {}",
                        blankToDefault(sparkApiConfig.getChatDomain(), "generalv3.5"), text.size());
                webSocket.send(JSON.toJSONString(chatRequest));
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JSONObject response = JSON.parseObject(text);
                    JSONObject header = response.getJSONObject("header");
                    Integer code = header == null ? null : header.getInteger("code");
                    if (code != null && code != 0) {
                        String message = header.getString("message");
                        log.warn("Spark WebSocket chat returned error. Code: {}, Message: {}, SID: {}",
                                code, message, header.getString("sid"));
                        failure.set(message == null || message.isBlank() ? "Spark WebSocket chat error: " + code : message);
                        done.countDown();
                        return;
                    }
                    JSONObject payload = response.getJSONObject("payload");
                    JSONObject choices = payload == null ? null : payload.getJSONObject("choices");
                    JSONArray textArray = choices == null ? null : choices.getJSONArray("text");
                    if (textArray != null) {
                        for (int i = 0; i < textArray.size(); i++) {
                            JSONObject item = textArray.getJSONObject(i);
                            String part = item.getString("content");
                            if (part != null) {
                                content.append(part);
                            }
                        }
                    }
                    Integer status = choices == null ? null : choices.getInteger("status");
                    if (Integer.valueOf(2).equals(status)) {
                        done.countDown();
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse Spark WebSocket chat response.", e);
                    failure.set("讯飞星火WebSocket响应解析失败");
                    done.countDown();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Integer code = response == null ? null : response.code();
                log.warn("Spark WebSocket chat connection failed. HttpCode: {}", code, t);
                failure.set("讯飞星火WebSocket连接失败");
                done.countDown();
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                done.countDown();
            }
        });

        try {
            if (!done.await(90, TimeUnit.SECONDS)) {
                webSocket.cancel();
                throw new IOException("讯飞星火WebSocket响应超时");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("讯飞星火WebSocket请求被中断", e);
        } finally {
            webSocket.close(1000, "completed");
        }

        if (failure.get() != null) {
            throw new IOException(failure.get());
        }
        String result = content.toString().trim();
        if (result.isEmpty()) {
            throw new IOException("讯飞星火WebSocket未返回有效内容");
        }
        return result;
    }

    private JSONObject buildChatRequest(String platformUserId, List<Message> messageHistory) {
        JSONObject root = new JSONObject();

        JSONObject header = new JSONObject();
        header.put("app_id", sparkApiConfig.getAppid());
        if (platformUserId != null && !platformUserId.isBlank()) {
            header.put("uid", platformUserId);
        }
        root.put("header", header);

        JSONObject chat = new JSONObject();
        chat.put("domain", blankToDefault(sparkApiConfig.getChatDomain(), "generalv3.5"));
        chat.put("temperature", 0.6);
        chat.put("max_tokens", 2048);
        JSONObject parameter = new JSONObject();
        parameter.put("chat", chat);
        root.put("parameter", parameter);

        JSONArray text = buildWebSocketMessages(messageHistory);
        JSONObject message = new JSONObject();
        message.put("text", text);
        JSONObject payload = new JSONObject();
        payload.put("message", message);
        root.put("payload", payload);
        return root;
    }

    private JSONArray buildWebSocketMessages(List<Message> messageHistory) {
        StringBuilder systemContext = new StringBuilder();
        JSONArray text = new JSONArray();

        if (messageHistory != null) {
            for (Message msg : messageHistory) {
                if (msg == null || msg.getContent() == null || msg.getContent().isBlank()) {
                    continue;
                }
                String role = msg.getRole();
                if ("system".equals(role)) {
                    if (!systemContext.isEmpty()) {
                        systemContext.append("\n\n");
                    }
                    systemContext.append(msg.getContent());
                    continue;
                }
                JSONObject item = new JSONObject();
                item.put("role", "assistant".equals(role) ? "assistant" : "user");
                item.put("content", msg.getContent());
                text.add(item);
            }
        }

        String context = systemContext.toString().trim();
        if (!context.isEmpty()) {
            String prefix = "系统设定：\n" + context + "\n\n";
            if (text.isEmpty()) {
                JSONObject item = new JSONObject();
                item.put("role", "user");
                item.put("content", prefix + "请开始。");
                text.add(item);
            } else {
                JSONObject first = text.getJSONObject(0);
                if ("user".equals(first.getString("role"))) {
                    first.put("content", prefix + first.getString("content"));
                } else {
                    JSONObject item = new JSONObject();
                    item.put("role", "user");
                    item.put("content", prefix + "以下是已开始的面试上下文，请继续保持同一场面试。");
                    text.add(0, item);
                }
            }
        }
        return text;
    }

    private String createChatAuthUrl() throws Exception {
        URI uri = URI.create(sparkApiConfig.getChatWsUrl());
        String host = uri.getHost();
        String path = uri.getRawPath();
        String date = DateTimeFormatter.RFC_1123_DATE_TIME
                .withLocale(Locale.US)
                .format(ZonedDateTime.now(ZoneOffset.UTC));
        String signatureOrigin = "host: " + host + "\n"
                + "date: " + date + "\n"
                + "GET " + path + " HTTP/1.1";
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(sparkApiConfig.getApisecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = Base64.getEncoder().encodeToString(
                mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8)));
        String authorizationOrigin = String.format(
                "api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"",
                sparkApiConfig.getApikey(), signature);
        String authorization = Base64.getEncoder().encodeToString(
                authorizationOrigin.getBytes(StandardCharsets.UTF_8));
        return sparkApiConfig.getChatWsUrl()
                + "?authorization=" + URLEncoder.encode(authorization, StandardCharsets.UTF_8)
                + "&date=" + URLEncoder.encode(date, StandardCharsets.UTF_8)
                + "&host=" + URLEncoder.encode(host, StandardCharsets.UTF_8);
    }

    private boolean hasWebSocketConfig() {
        return sparkApiConfig.getChatWsUrl() != null && !sparkApiConfig.getChatWsUrl().isBlank()
                && sparkApiConfig.getAppid() != null && !sparkApiConfig.getAppid().isBlank()
                && sparkApiConfig.getApikey() != null && !sparkApiConfig.getApikey().isBlank()
                && sparkApiConfig.getApisecret() != null && !sparkApiConfig.getApisecret().isBlank();
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
