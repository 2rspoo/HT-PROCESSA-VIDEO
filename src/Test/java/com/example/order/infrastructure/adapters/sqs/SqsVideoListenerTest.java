package com.example.order.infrastructure.adapters.sqs;

import com.example.order.application.ports.in.ProcessVideoCommand;
import com.example.order.domain.entities.VideoMetadata;
import com.example.order.infrastructure.adapters.sqs.dto.VideoEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SqsVideoListenerTest {

    @Mock
    private ProcessVideoCommand processVideoCommand;

    @InjectMocks
    private SqsVideoListener listener;

    @Test
    @DisplayName("Deve converter VideoEvent para VideoMetadata e chamar o comando de processamento")
    void shouldReceiveMessageAndCallUseCase() {
        // Dado
        VideoEvent event = new VideoEvent("video-123", "user-456", "meu_video.mp4");

        // Execução
        listener.onMessage(event);

        // Verificação
        ArgumentCaptor<VideoMetadata> captor = ArgumentCaptor.forClass(VideoMetadata.class);
        verify(processVideoCommand).process(captor.capture());

        VideoMetadata capturedDomain = captor.getValue();

        assertEquals("video-123", capturedDomain.pedidoId());
        assertEquals("user-456", capturedDomain.userId());
        assertEquals("meu_video.mp4", capturedDomain.fileName());
        assertEquals("RECEIVED", capturedDomain.status());
    }
}