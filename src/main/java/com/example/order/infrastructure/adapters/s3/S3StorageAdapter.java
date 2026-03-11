package com.example.order.infrastructure.adapters.s3;

import com.example.order.application.ports.out.VideoStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

// infrastructure/adapters/s3/S3StorageAdapter.java
@Component
public class S3StorageAdapter implements VideoStoragePort {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3StorageAdapter(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void upload(String videoId, byte[] data) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key("uploads/" + videoId)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(data));
    }
}