package com.example.order.application.usecases;

import com.example.order.application.ports.out.NotificationPort;
import com.example.order.application.ports.out.VideoRepositoryPort;
import com.example.order.application.ports.out.VideoResultPort;
import com.example.order.application.ports.out.VideoStoragePort;
import com.example.order.domain.entities.VideoMetadata;
import com.example.order.infrastructure.adapters.video.FFmpegVideoProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessVideoUseCaseTest {

    @Mock
    private VideoRepositoryPort repository; // Nome igual ao da sua classe real

    @Mock
    private VideoStoragePort storage; // Nome igual ao da sua classe real

    @Mock
    private VideoResultPort result; // Nome igual ao da sua classe real

    @Mock
    private NotificationPort notification; // Nome igual ao da sua classe real

    @Mock
    private FFmpegVideoProcessor processor; // O componente que estava faltando!

    @InjectMocks
    private ProcessVideoUseCase useCase;

    private VideoMetadata videoMetadata;

    @BeforeEach
    void setUp() {
        videoMetadata = new VideoMetadata(
                "video-123",
                "user-456",
                "original-video.mp4",
                "RECEIVED",
                null,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Deve executar o fluxo completo de processamento com sucesso")
    void shouldProcessVideoSuccessfully() throws Exception {
        // 1. Configurar os Mocks
        byte[] fakeVideoData = new byte[]{1, 2, 3};
        File fakeZipFile = new File("resultado.zip");
        String fakeS3Url = "s3://meu-bucket/video-123.zip";

        when(storage.download(videoMetadata.pedidoId())).thenReturn(fakeVideoData);
        when(processor.process(fakeVideoData, videoMetadata.fileName())).thenReturn(fakeZipFile);
        when(storage.uploadZip(videoMetadata.pedidoId() + ".zip", fakeZipFile)).thenReturn(fakeS3Url);

        // 2. Execução
        useCase.process(videoMetadata);

        // 3. Verificações (Verifica exatamente a ordem do seu código)
        verify(repository, times(1)).updateStatus(videoMetadata.pedidoId(), "PROCESSING");
        verify(storage, times(1)).download(videoMetadata.pedidoId());
        verify(processor, times(1)).process(fakeVideoData, videoMetadata.fileName());
        verify(storage, times(1)).uploadZip(videoMetadata.pedidoId() + ".zip", fakeZipFile);
        verify(result, times(1)).sendToProcess(any(VideoMetadata.class));
        verify(notification, times(1)).sendNotification(videoMetadata.pedidoId(), "DONE", fakeS3Url);
    }

    @Test
    @DisplayName("Deve capturar a exceção e atualizar o status para ERROR quando falhar")
    void shouldHandleExceptionAndSetStatusToError() throws Exception {
        // Configuração: Simula um erro logo na hora de baixar do S3
        doThrow(new RuntimeException("S3 fora do ar")).when(storage).download(anyString());

        // Execução
        useCase.process(videoMetadata);

        // Verificações
        // Ele deve ter atualizado pra PROCESSING no início do método
        verify(repository, times(1)).updateStatus(videoMetadata.pedidoId(), "PROCESSING");

        // E como deu erro no download, deve ter caído no catch e atualizado pra ERROR
        verify(repository, times(1)).updateStatus(videoMetadata.pedidoId(), "ERROR");

        // Garante que o resto do fluxo NUNCA foi chamado
        verify(processor, never()).process(any(byte[].class), anyString());
        verify(storage, never()).uploadZip(anyString(), any(File.class));
        verify(result, never()).sendToProcess(any(VideoMetadata.class));
        verify(notification, never()).sendNotification(anyString(), anyString(), anyString());
    }
}