package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.dto.ai.MultimodalTextRequest;
import com.example.entity.Account;
import com.example.entity.MultimodalRecord;
import com.example.service.MultimodalRecordService;
import com.example.service.SparkTtsService;
import com.example.utils.TokenUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MultimodalControllerTest {

    @Test
    void codeRunRejectsBlankCode() {
        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            MultimodalController controller = new MultimodalController();
            MultimodalTextRequest request = new MultimodalTextRequest();
            request.setCode(" ");

            Result result = controller.codeRun(request);

            assertThat(result.getCode()).isEqualTo("400");
        }
    }

    @Test
    void codeRunReturnsReservedPayloadWithoutExecutingCode() {
        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            MultimodalController controller = new MultimodalController();
            MultimodalRecordService recordService = mock(MultimodalRecordService.class);
            MultimodalRecord record = new MultimodalRecord();
            record.setId(99L);
            when(recordService.createCodeRecord(any(Account.class), eq(null), eq("java"), eq("System.out.println(\"hello\");")))
                    .thenReturn(record);
            ReflectionTestUtils.setField(controller, "recordService", recordService);
            MultimodalTextRequest request = new MultimodalTextRequest();
            request.setLanguage("java");
            request.setCode("System.out.println(\"hello\");");

            Result result = controller.codeRun(request);

            assertThat(result.getCode()).isEqualTo("200");
            assertThat(result.getData()).isInstanceOf(Map.class);
            Map<?, ?> payload = (Map<?, ?>) result.getData();
            assertThat(payload.get("recordId")).isEqualTo(99L);
            assertThat(payload.get("status")).isEqualTo("RESERVED");
            assertThat(payload.get("runOutput")).isEqualTo("");
        }
    }

    @Test
    void codeRunNormalizesBlankLanguageInPayload() {
        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            MultimodalController controller = new MultimodalController();
            MultimodalRecordService recordService = mock(MultimodalRecordService.class);
            MultimodalRecord record = new MultimodalRecord();
            record.setId(100L);
            when(recordService.createCodeRecord(any(Account.class), eq(null), eq("  "), eq("return 1;")))
                    .thenReturn(record);
            ReflectionTestUtils.setField(controller, "recordService", recordService);
            MultimodalTextRequest request = new MultimodalTextRequest();
            request.setLanguage("  ");
            request.setCode("return 1;");

            Result result = controller.codeRun(request);

            Map<?, ?> payload = (Map<?, ?>) result.getData();
            assertThat(payload.get("language")).isEqualTo("unknown");
        }
    }

    @Test
    void asrReturnsReservedRecordForUploadedAudio() {
        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            MultimodalController controller = new MultimodalController();
            MultimodalRecordService recordService = mock(MultimodalRecordService.class);
            MultimodalRecord record = new MultimodalRecord();
            record.setId(101L);
            when(recordService.createAudioRecord(any(Account.class), eq("session-1"), eq("answer.wav"), eq(4L)))
                    .thenReturn(record);
            ReflectionTestUtils.setField(controller, "recordService", recordService);
            MockMultipartFile file = new MockMultipartFile(
                    "file", "answer.wav", "audio/wav", new byte[]{1, 2, 3, 4});

            Result result = controller.asr("session-1", file);

            assertThat(result.getCode()).isEqualTo("200");
            assertThat(result.getData()).isInstanceOf(com.example.dto.ai.MultimodalResult.class);
            com.example.dto.ai.MultimodalResult payload =
                    (com.example.dto.ai.MultimodalResult) result.getData();
            assertThat(payload.getRecordId()).isEqualTo(101L);
            assertThat(payload.getModality()).isEqualTo("AUDIO");
            assertThat(payload.getStatus()).isEqualTo("RESERVED");
            assertThat(payload.getTranscript()).isEqualTo("");
        }
    }

    @Test
    void videoAnalysisReturnsReservedRecordForUploadedVideo() {
        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            MultimodalController controller = new MultimodalController();
            MultimodalRecordService recordService = mock(MultimodalRecordService.class);
            MultimodalRecord record = new MultimodalRecord();
            record.setId(102L);
            when(recordService.createVideoRecord(any(Account.class), eq("session-2"), eq("clip.mp4"), eq(3L)))
                    .thenReturn(record);
            ReflectionTestUtils.setField(controller, "recordService", recordService);
            MockMultipartFile file = new MockMultipartFile(
                    "file", "clip.mp4", "video/mp4", new byte[]{1, 2, 3});

            Result result = controller.videoAnalysis("session-2", file);

            assertThat(result.getCode()).isEqualTo("200");
            assertThat(result.getData()).isInstanceOf(com.example.dto.ai.MultimodalResult.class);
            com.example.dto.ai.MultimodalResult payload =
                    (com.example.dto.ai.MultimodalResult) result.getData();
            assertThat(payload.getRecordId()).isEqualTo(102L);
            assertThat(payload.getModality()).isEqualTo("VIDEO");
            assertThat(payload.getStatus()).isEqualTo("RESERVED");
        }
    }

    @Test
    void ttsReturnsMpegAudioFromProvider() throws Exception {
        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            MultimodalController controller = new MultimodalController();
            SparkTtsService ttsService = mock(SparkTtsService.class);
            when(ttsService.synthesize("hello")).thenReturn(new byte[]{1, 2, 3});
            ReflectionTestUtils.setField(controller, "sparkTtsService", ttsService);
            MultimodalTextRequest request = new MultimodalTextRequest();
            request.setText(" hello ");

            ResponseEntity<byte[]> response = controller.tts(request);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getHeaders().getContentType().toString()).isEqualTo("audio/mpeg");
            assertThat(response.getBody()).containsExactly(1, 2, 3);
        }
    }

    @Test
    void ttsReturnsBadGatewayWhenProviderFails() throws Exception {
        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            MultimodalController controller = new MultimodalController();
            SparkTtsService ttsService = mock(SparkTtsService.class);
            when(ttsService.synthesize("hello")).thenThrow(new java.io.IOException("provider down"));
            ReflectionTestUtils.setField(controller, "sparkTtsService", ttsService);
            MultimodalTextRequest request = new MultimodalTextRequest();
            request.setText("hello");

            ResponseEntity<byte[]> response = controller.tts(request);

            assertThat(response.getStatusCode().value()).isEqualTo(502);
        }
    }

    @Test
    void ttsRejectsBlankTextBeforeCallingProvider() {
        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            MultimodalController controller = new MultimodalController();
            MultimodalTextRequest request = new MultimodalTextRequest();
            request.setText(" ");

            ResponseEntity<byte[]> response = controller.tts(request);

            assertThat(response.getStatusCode().value()).isEqualTo(400);
        }
    }

    @Test
    void listBySessionReturnsCurrentUserRecords() {
        try (MockedStatic<TokenUtils> tokenUtils = mockUser()) {
            MultimodalController controller = new MultimodalController();
            MultimodalRecordService recordService = mock(MultimodalRecordService.class);
            MultimodalRecord record = new MultimodalRecord();
            record.setId(100L);
            record.setSessionId("session-1");
            record.setModality("CODE");
            when(recordService.listBySession(any(Account.class), eq("session-1"))).thenReturn(List.of(record));
            ReflectionTestUtils.setField(controller, "recordService", recordService);

            Result result = controller.listBySession("session-1");

            assertThat(result.getCode()).isEqualTo("200");
            assertThat(result.getData()).isEqualTo(List.of(record));
        }
    }

    private MockedStatic<TokenUtils> mockUser() {
        Account account = new Account();
        account.setId(1);
        account.setRole(RoleEnum.USER.name());
        MockedStatic<TokenUtils> tokenUtils = mockStatic(TokenUtils.class);
        tokenUtils.when(TokenUtils::getCurrentUser).thenReturn(account);
        return tokenUtils;
    }
}
