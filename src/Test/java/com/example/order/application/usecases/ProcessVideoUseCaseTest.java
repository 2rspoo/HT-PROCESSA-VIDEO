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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessVideoUseCaseTest {

    @Mock
    private VideoRepositoryPort repository;

    @Mock
    private VideoStoragePort storage;

    @Mock
    private VideoResultPort result;

    @Mock
    private NotificationPort notification;

    @Mock
    private FFmpegVideoProcessor processor;

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
        // Criamos um arquivo temporário real para evitar problemas de IO no processor
        File fakeZipFile = File.createTempFile("resultado", ".zip");
        String fakeS3Url = "s3://meu-bucket/video-123.zip";

        // Garantir que os stubs retornem valores que permitam o fluxo continuar
        when(storage.download(anyString())).thenReturn(fakeVideoData);
        when(processor.process(any(byte[].class), anyString())).thenReturn(fakeZipFile);
        when(storage.uploadZip(anyString(), any(File.class))).thenReturn(fakeS3Url);

        // Se o seu useCase chama result.sendToProcess, não precisamos de 'when' pois é void,
        // mas precisamos garantir que ele não lance exceção (padrão do Mockito).

        // 2. Execução
        useCase.process(videoMetadata);

        // 3. Verificações
        verify(repository).updateStatus(videoMetadata.pedidoId(), "PROCESSING");
        verify(storage).download(videoMetadata.pedidoId());
        verify(processor).process(eq(fakeVideoData), eq(videoMetadata.fileName()));
        verify(storage).uploadZip(contains("video-123"), any(File.class));
        verify(notification).sendNotification(eq("video-123"), eq("DONE"), eq(fakeS3Url));

        fakeZipFile.deleteOnExit();
    }

    @Test
    @DisplayName("Deve capturar a exceção e atualizar o status para ERROR quando falhar")
    void shouldHandleExceptionAndSetStatusToError() throws Exception {
        // Configuração: Simula erro no download
        // Importante: use 'doThrow' para métodos que podem lançar checked exceptions
        doThrow(new RuntimeException("S3 fora do ar")).when(storage).download(anyString());

        // Execução
        useCase.process(videoMetadata);

        // Verificações
        // 1. Iniciou o processo
        verify(repository).updateStatus(videoMetadata.pedidoId(), "PROCESSING");

        // 2. Terminou em erro (O erro que você estava tendo era que o Mockito não via essa chamada)
        // Certifique-se que no UseCase o updateStatus("ERROR") está dentro do catch(Exception e)
        verify(repository).updateStatus(videoMetadata.pedidoId(), "ERROR");

        // 3. Garante que não notificou sucesso
        verify(notification, never()).sendNotification(anyString(), anyString(), anyString());
    }

}