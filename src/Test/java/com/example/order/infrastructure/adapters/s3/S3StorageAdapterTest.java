package com.example.order.infrastructure.adapters.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.S3Utilities;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3StorageAdapterTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Utilities s3Utilities;

    private S3StorageAdapter adapter;
    private final String bucketName = "video-bucket";

    @BeforeEach
    void setUp() {
        adapter = new S3StorageAdapter(s3Client, bucketName);
    }

    @Test
    @DisplayName("Deve realizar download de arquivo da pasta uploads")
    void shouldDownloadFileSuccessfully() {
        String fileId = "video123.mp4";
        byte[] expectedContent = "video content".getBytes();

        // Mock do retorno do S3
        ResponseBytes<GetObjectResponse> responseBytes = mock(ResponseBytes.class);
        when(responseBytes.asByteArray()).thenReturn(expectedContent);
        when(s3Client.getObjectAsBytes(any(Consumer.class))).thenReturn(responseBytes);

        // Execução
        byte[] result = adapter.download(fileId);

        // Verificação
        assertArrayEquals(expectedContent, result);
        verify(s3Client).getObjectAsBytes(any(Consumer.class));
    }

    @Test
    @DisplayName("Deve realizar upload do ZIP para a pasta processed e retornar a URL")
    void shouldUploadZipSuccessfully(@TempDir Path tempDir) throws IOException {
        String fileName = "result.zip";
        File tempFile = Files.createFile(tempDir.resolve(fileName)).toFile();
        String expectedUrl = "https://s3.amazonaws.com/video-bucket/processed/result.zip";

        // Mocks complexos para o getUrl do SDK v2
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(new URL(expectedUrl));
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        // Execução
        String resultUrl = adapter.uploadZip(fileName, tempFile);

        // Verificações
        assertEquals(expectedUrl, resultUrl);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        assertEquals(bucketName, requestCaptor.getValue().bucket());
        assertEquals("processed/" + fileName, requestCaptor.getValue().key());
    }
}