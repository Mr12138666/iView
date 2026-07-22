package com.example.controller;

import com.example.common.Result;
import com.example.service.MinioFileService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileControllerTest {

    @Test
    void uploadReturnsMinioPublicUrl() throws Exception {
        FileController controller = new FileController();
        MinioFileService fileService = mock(MinioFileService.class);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1});
        when(fileService.upload(file, "common")).thenReturn("http://120.53.242.78:19000/job-assets/common/avatar.png");
        ReflectionTestUtils.setField(controller, "minioFileService", fileService);

        Result result = controller.upload(file);

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isEqualTo("http://120.53.242.78:19000/job-assets/common/avatar.png");
    }

    @Test
    void uploadAdvertiseReturnsMinioPublicUrl() throws Exception {
        FileController controller = new FileController();
        MinioFileService fileService = mock(MinioFileService.class);
        MockMultipartFile file = new MockMultipartFile("file", "banner.png", "image/png", new byte[]{1});
        when(fileService.upload(file, "advertise")).thenReturn("http://120.53.242.78:19000/job-assets/advertise/banner.png");
        ReflectionTestUtils.setField(controller, "minioFileService", fileService);

        Result result = controller.uploadAdvertise(file);

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isEqualTo("http://120.53.242.78:19000/job-assets/advertise/banner.png");
    }

    @Test
    void wangEditorUploadReturnsMinioPublicUrlPayload() throws Exception {
        FileController controller = new FileController();
        MinioFileService fileService = mock(MinioFileService.class);
        MockMultipartFile file = new MockMultipartFile("file", "content.png", "image/png", new byte[]{1});
        when(fileService.upload(file, "editor")).thenReturn("http://120.53.242.78:19000/job-assets/editor/content.png");
        ReflectionTestUtils.setField(controller, "minioFileService", fileService);

        Map<String, Object> result = controller.wangEditorUpload(file);

        assertThat(result.get("errno")).isEqualTo(0);
        List<?> data = (List<?>) result.get("data");
        Map<?, ?> first = (Map<?, ?>) data.get(0);
        assertThat(first.get("url")).isEqualTo("http://120.53.242.78:19000/job-assets/editor/content.png");
    }
}
