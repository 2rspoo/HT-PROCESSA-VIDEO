package com.example.order.application.services;

import com.example.order.application.ports.in.ProcessVideoCommand;
import com.example.order.application.ports.out.*; // Portas de saída hipotéticas
import com.example.order.domain.entities.VideoMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Teste para a implementação da porta de entrada ProcessVideoCommand.
 * Verifica se o fluxo: Download -> Prints -> Zip -> Upload -> Notificação
 * é executado na ordem correta.
 */
@ExtendWith(MockitoExtension.class)
class ProcessVideoUseCaseTest {

    @Mock
    private VideoDownloadPort downloadPort;
    @Mock
    private VideoProcessingPort processingPort;
    @Mock
    private VideoStoragePort storagePort;
    @Mock
    private NotificationPort notificationPort;

    @InjectMocks
    private ProcessVideoUseCase useCase;

    @Test
    @DisplayName("Deve executar todo o fluxo de processamento na ordem correta")
    void shouldExecuteFullProcessFlowInOrder() {
        // Arrange
        VideoMetadata metadata = new VideoMetadata("id-123", "video.mp4", "user@email.com");

        // Act
        useCase.process(metadata);

        // Assert - Verificação de Ordem (InOrder)
        InOrder inOrder = inOrder(downloadPort, processingPort, storagePort, notificationPort);

        // 1. Download
        inOrder.verify(downloadPort).download(metadata);
        // 2. Prints & Zip (Processamento)
        inOrder.verify(processingPort).generatePrints(any());
        inOrder.verify(processingPort).zipResult(any());
        // 3. Upload (Storage)
        inOrder.verify(storagePort).upload(any());
        // 4. Notificação
        inOrder.verify(notificationPort).sendNotification(eq("user@email.com"), anyString());
    }

    @Test
    @DisplayName("Deve interromper o fluxo se o download falhar")
    void shouldStopFlowIfDownloadFails() {
        // Arrange
        VideoMetadata metadata = new VideoMetadata("id-123", "erro.mp4", "user@email.com");
        doThrow(new RuntimeException("Falha no download")).when(downloadPort).download(metadata);

        // Act & Assert (Opcional: assertThrows se o service relançar a exceção)
        try {
            useCase.process(metadata);
        } catch (Exception ignored) {}

        // Verifica que os passos seguintes NUNCA foram chamados
        verify(processingPort, never()).generatePrints(any());
        verify(storagePort, never()).upload(any());
        verify(notificationPort, never()).sendNotification(anyString(), anyString());
    }
}

// --- Exemplo de Implementação (Caso de Uso) para o teste passar ---

class ProcessVideoUseCase implements ProcessVideoCommand {
    private final VideoDownloadPort downloadPort;
    private final VideoProcessingPort processingPort;
    private final VideoStoragePort storagePort;
    private final NotificationPort notificationPort;

    public ProcessVideoUseCase(VideoDownloadPort downloadPort, VideoProcessingPort processingPort,
                               VideoStoragePort storagePort, NotificationPort notificationPort) {
        this.downloadPort = downloadPort;
        this.processingPort = processingPort;
        this.storagePort = storagePort;
        this.notificationPort = notificationPort;
    }

    @Override
    public void process(VideoMetadata metadata) {
        // Execução do fluxo coordenado
        downloadPort.download(metadata);

        var processedContent = processingPort.generatePrints(metadata);
        var zippedFile = processingPort.zipResult(processedContent);

        storagePort.upload(zippedFile);

        notificationPort.sendNotification(metadata.getUserEmail(), "Seu vídeo foi processado com sucesso!");
    }
}