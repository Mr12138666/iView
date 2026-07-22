package com.example.service;

import com.example.common.config.SparkApiConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SparkTtsServiceTest {

    @Test
    void createAuthUrlSupportsWssScheme() {
        SparkApiConfig config = new SparkApiConfig();
        config.setTtsWsUrl("wss://tts-api.xfyun.cn/v2/tts");
        config.setApikey("api-key");
        config.setApisecret("api-secret");
        SparkTtsService service = new SparkTtsService(config);

        String authUrl = ReflectionTestUtils.invokeMethod(service, "createAuthUrl");

        assertThat(authUrl).startsWith("wss://tts-api.xfyun.cn/v2/tts?");
        assertThat(authUrl).contains("authorization=");
        assertThat(authUrl).contains("host=tts-api.xfyun.cn");
    }
}
