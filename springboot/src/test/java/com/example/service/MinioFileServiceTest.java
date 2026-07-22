package com.example.service;

import com.example.common.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MinioFileServiceTest {

    @Test
    void uploadStoresFileInDirectoryAndReturnsPublicUrl() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        MinioProperties properties = new MinioProperties();
        properties.setBucket("job-assets");
        properties.setPublicUrl("http://120.53.242.78:19000/job-assets");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        MinioFileService service = new MinioFileService(minioClient, properties);
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", new byte[]{1, 2, 3});

        String url = service.upload(file, "avatar");

        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());
        ArgumentCaptor<SetBucketPolicyArgs> policyCaptor = ArgumentCaptor.forClass(SetBucketPolicyArgs.class);
        verify(minioClient).setBucketPolicy(policyCaptor.capture());
        PutObjectArgs args = captor.getValue();
        assertThat(args.bucket()).isEqualTo("job-assets");
        assertThat(args.object()).startsWith("avatar/").endsWith("avatar.png");
        assertThat(policyCaptor.getValue().bucket()).isEqualTo("job-assets");
        assertThat(url).isEqualTo("http://120.53.242.78:19000/job-assets/" + args.object());
    }

    @Test
    void uploadUsesCommonDirectoryWhenDirectoryIsBlank() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        MinioProperties properties = new MinioProperties();
        properties.setBucket("job-assets");
        properties.setPublicUrl("http://120.53.242.78:19000/job-assets/");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        MinioFileService service = new MinioFileService(minioClient, properties);
        MockMultipartFile file = new MockMultipartFile(
                "file", "company logo.png", null, new byte[]{1});

        String url = service.upload(file, " ");

        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());
        PutObjectArgs args = captor.getValue();
        assertThat(args.object()).startsWith("common/").endsWith("company-logo.png");
        assertThat(url).isEqualTo("http://120.53.242.78:19000/job-assets/" + args.object());
    }
}
