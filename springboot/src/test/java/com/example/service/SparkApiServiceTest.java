package com.example.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.common.config.SparkApiConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SparkApiServiceTest {

    @Test
    void buildChatRequestMergesSystemPromptIntoFirstUserMessage() {
        SparkApiConfig config = new SparkApiConfig();
        config.setAppid("app-id");
        config.setChatDomain("generalv3.5");
        SparkApiService service = new SparkApiService(config);

        JSONObject request = ReflectionTestUtils.invokeMethod(service, "buildChatRequest", "user-1", List.of(
                SparkApiService.Message.builder().role("system").content("Interview as senior Java interviewer.").build(),
                SparkApiService.Message.builder().role("user").content("Start the interview.").build()
        ));

        JSONArray text = request.getJSONObject("payload")
                .getJSONObject("message")
                .getJSONArray("text");

        assertThat(text).hasSize(1);
        assertThat(text.getJSONObject(0).getString("role")).isEqualTo("user");
        assertThat(text.getJSONObject(0).getString("content"))
                .contains("Interview as senior Java interviewer.")
                .contains("Start the interview.");
    }

    @Test
    void buildChatRequestPreservesStoredAssistantHistoryAfterSystemPrompt() {
        SparkApiConfig config = new SparkApiConfig();
        config.setAppid("app-id");
        SparkApiService service = new SparkApiService(config);

        JSONObject request = ReflectionTestUtils.invokeMethod(service, "buildChatRequest", "user-1", List.of(
                SparkApiService.Message.builder().role("system").content("Keep asking one question at a time.").build(),
                SparkApiService.Message.builder().role("assistant").content("First question?").build(),
                SparkApiService.Message.builder().role("user").content("My answer.").build()
        ));

        JSONArray text = request.getJSONObject("payload")
                .getJSONObject("message")
                .getJSONArray("text");

        assertThat(text).hasSize(3);
        assertThat(text.getJSONObject(0).getString("role")).isEqualTo("user");
        assertThat(text.getJSONObject(0).getString("content"))
                .contains("Keep asking one question at a time.");
        assertThat(text.getJSONObject(1).getString("role")).isEqualTo("assistant");
        assertThat(text.getJSONObject(2).getString("role")).isEqualTo("user");
    }

    @Test
    void createChatAuthUrlSupportsWssScheme() {
        SparkApiConfig config = new SparkApiConfig();
        config.setChatWsUrl("wss://spark-api.xf-yun.com/v3.5/chat");
        config.setApikey("api-key");
        config.setApisecret("api-secret");
        SparkApiService service = new SparkApiService(config);

        String authUrl = ReflectionTestUtils.invokeMethod(service, "createChatAuthUrl");

        assertThat(authUrl).startsWith("wss://spark-api.xf-yun.com/v3.5/chat?");
        assertThat(authUrl).contains("authorization=");
        assertThat(authUrl).contains("host=spark-api.xf-yun.com");
    }
}
