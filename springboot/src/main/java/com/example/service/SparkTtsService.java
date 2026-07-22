package com.example.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.common.config.SparkApiConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 讯飞 TTS 服务端代理。
 *
 * <p>讯飞密钥只在服务端使用，浏览器不会拿到 app secret。</p>
 */
@Service
public class SparkTtsService {

    private static final int TIMEOUT_SECONDS = 30;
    private final SparkApiConfig config;
    private final OkHttpClient client;

    public SparkTtsService(SparkApiConfig config) {
        this.config = config;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public byte[] synthesize(String text) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("文本不能为空");
        }
        if (config.getTtsWsUrl() == null || config.getTtsWsUrl().isBlank()
                || config.getAppid() == null || config.getAppid().isBlank()
                || config.getApikey() == null || config.getApikey().isBlank()
                || config.getApisecret() == null || config.getApisecret().isBlank()) {
            throw new IllegalStateException("TTS服务未配置");
        }

        CountDownLatch done = new CountDownLatch(1);
        ByteArrayOutputStream audio = new ByteArrayOutputStream();
        AtomicBoolean completed = new AtomicBoolean(false);
        AtomicReference<String> failure = new AtomicReference<>();
        Request request = new Request.Builder().url(createAuthUrl()).build();

        WebSocket webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                webSocket.send(JSON.toJSONString(buildRequest(text)));
            }

            @Override
            public void onMessage(WebSocket webSocket, String textMessage) {
                try {
                    JSONObject response = JSON.parseObject(textMessage);
                    Integer code = response.getInteger("code");
                    if (code != null && code != 0) {
                        failure.set(response.getString("message"));
                        done.countDown();
                        return;
                    }
                    JSONObject data = response.getJSONObject("data");
                    if (data == null) {
                        return;
                    }
                    String audioBase64 = data.getString("audio");
                    if (audioBase64 != null && !audioBase64.isEmpty()) {
                        audio.write(Base64.getDecoder().decode(audioBase64));
                    }
                    if (Integer.valueOf(2).equals(data.getInteger("status"))) {
                        completed.set(true);
                        done.countDown();
                    }
                } catch (Exception e) {
                    failure.set("TTS响应解析失败");
                    done.countDown();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                failure.set("TTS连接失败");
                done.countDown();
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                if (!completed.get()) {
                    failure.compareAndSet(null, "TTS连接提前关闭");
                    done.countDown();
                }
            }
        });

        if (!done.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            webSocket.cancel();
            throw new IOException("TTS服务响应超时");
        }
        webSocket.close(1000, "completed");
        if (failure.get() != null) {
            throw new IOException(failure.get());
        }
        if (audio.size() == 0) {
            throw new IOException("TTS未返回音频数据");
        }
        return audio.toByteArray();
    }

    private Object buildRequest(String text) {
        JSONObject root = new JSONObject();
        JSONObject common = new JSONObject();
        common.put("app_id", config.getAppid());
        root.put("common", common);

        JSONObject business = new JSONObject();
        business.put("aue", "lame");
        business.put("sfl", 1);
        business.put("auf", "audio/L16;rate=16000");
        business.put("vcn", "x4_yezi");
        business.put("speed", 50);
        business.put("volume", 50);
        business.put("pitch", 50);
        business.put("bgs", 0);
        business.put("reg", "0");
        business.put("rdn", "0");
        business.put("rhy", "0");
        business.put("tte", "utf8");
        root.put("business", business);

        JSONObject data = new JSONObject();
        data.put("status", 2);
        data.put("text", Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8)));
        root.put("data", data);
        return root;
    }

    private String createAuthUrl() throws Exception {
        URI uri = URI.create(config.getTtsWsUrl());
        String host = uri.getHost();
        String path = uri.getRawPath();
        if (path == null || path.isBlank()) {
            path = "/";
        }
        if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
            path += "?" + uri.getRawQuery();
        }
        String date = DateTimeFormatter.RFC_1123_DATE_TIME
                .withLocale(Locale.US)
                .format(ZonedDateTime.now(ZoneOffset.UTC));
        String signatureOrigin = "host: " + host + "\n"
                + "date: " + date + "\n"
                + "GET " + path + " HTTP/1.1";
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(config.getApisecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = Base64.getEncoder().encodeToString(
                mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8)));
        String authorizationOrigin = String.format(
                "api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"",
                config.getApikey(), signature);
        String authorization = Base64.getEncoder().encodeToString(
                authorizationOrigin.getBytes(StandardCharsets.UTF_8));
        return config.getTtsWsUrl()
                + "?authorization=" + URLEncoder.encode(authorization, StandardCharsets.UTF_8)
                + "&date=" + URLEncoder.encode(date, StandardCharsets.UTF_8)
                + "&host=" + URLEncoder.encode(host, StandardCharsets.UTF_8);
    }
}
