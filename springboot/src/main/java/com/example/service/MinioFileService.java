package com.example.service;

import cn.hutool.core.util.StrUtil;
import com.example.common.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class MinioFileService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public MinioFileService(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    public String upload(MultipartFile file, String directory) throws Exception {
        ensureBucketReady();
        String objectName = buildObjectName(file.getOriginalFilename(), directory);
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(StrUtil.blankToDefault(file.getContentType(), "application/octet-stream"))
                .build());
        return buildPublicUrl(objectName);
    }

    private void ensureBucketReady() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(properties.getBucket())
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(properties.getBucket())
                    .build());
        }
        minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                .bucket(properties.getBucket())
                .config(buildPublicReadPolicy())
                .build());
    }

    private String buildObjectName(String originalFilename, String directory) {
        String safeDirectory = StrUtil.blankToDefault(directory, "common").trim().replaceAll("^/+|/+$", "");
        String safeFileName = StrUtil.blankToDefault(originalFilename, "file")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-zA-Z0-9._-]", "-");
        return safeDirectory + "/" + System.currentTimeMillis() + "-" + UUID.randomUUID() + "-" + safeFileName;
    }

    private String buildPublicUrl(String objectName) {
        return properties.getPublicUrl().replaceAll("/+$", "") + "/" + objectName;
    }

    private String buildPublicReadPolicy() {
        return """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(properties.getBucket());
    }
}
