package com.example.order.infrastructure.adapters.s3;

import com.example.order.application.ports.out.VideoStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.nio.file.Files;

@Component
public class S3StorageAdapter implements VideoStoragePort {

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageAdapter(S3Client s3Client, @Value("${AWS_S3_BUCKET}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public byte[] download(String fileId) {
        // É O ADAPTADOR que sabe que o S3 usa a pasta "uploads/"
        System.out.println(">>> Chama fileId " + fileId);
        String s3Key = "uploads/" + fileId;
        System.out.println(">>> Chama s3Key " + s3Key);

        return s3Client.getObjectAsBytes(builder -> builder.bucket(bucketName).key(s3Key)).asByteArray();
    }

    @Override
    public String uploadZip(String fileName, File file) {
        // 1. Faz o upload do arquivo ZIP
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key("processed/" + fileName) // Salva em uma "pasta" diferente no S3
                        .build(),
                RequestBody.fromFile(file));

        // 2. Retorna a URL pública (ou pré-assinada) do arquivo
        return s3Client.utilities().getUrl(GetUrlRequest.builder()
                .bucket(bucketName)
                .key("processed/" + fileName)
                .build()).toString();
    }
}