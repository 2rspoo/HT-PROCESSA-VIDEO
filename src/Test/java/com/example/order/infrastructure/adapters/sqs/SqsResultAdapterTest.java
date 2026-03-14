package com.example.order.infrastructure.adapters.sqs;

import com.example.order.domain.entities.VideoMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsResultAdapterTest {

    @Mock
    private SqsTemplate sqsTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SqsResultAdapter adapter;

    private VideoMetadata videoMetadata;
    private final String FAKE_QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/123456789/my-results-queue";

    @BeforeEach
    void setUp() {
        // Injeta o valor da variável de ambiente @Value manualmente para o teste
        ReflectionTestUtils.setField(adapter, "resultsQueueUrl", FAKE_QUEUE_URL);

        videoMetadata = new VideoMetadata(
                "video-123",
                "user-456",
                "original.mp4",
                "DONE",
                "s3://bucket/video-123.zip",
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Deve converter o objeto para JSON e enviar para a fila SQS com sucesso")
    void shouldSendToProcessSuccessfully() throws Exception {
        // Configuração
        String fakeJson = "{\"pedidoId\":\"video-123\",\"status\":\"DONE\"}";
        when(objectMapper.writeValueAsString(videoMetadata)).thenReturn(fakeJson);

        // Execução
        adapter.sendToProcess(videoMetadata);

        // Verificação
        verify(objectMapper, times(1)).writeValueAsString(videoMetadata);
        verify(sqsTemplate, times(1)).send(eq(FAKE_QUEUE_URL), eq(fakeJson));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando o ObjectMapper falhar ao converter para JSON")
    void shouldThrowExceptionWhenJsonProcessingFails() throws Exception {
        // Configuração: Simula um erro na hora de transformar o objeto em JSON
        JsonProcessingException mockException = mock(JsonProcessingException.class);
        when(objectMapper.writeValueAsString(any(VideoMetadata.class))).thenThrow(mockException);

        // Execução e Verificação
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adapter.sendToProcess(videoMetadata);
        });

        // Valida se a mensagem de erro está correta e se envelopou a causa original
        assertTrue(exception.getMessage().contains("Erro ao publicar resultado do processamento"));

        // Garante que o template do SQS NUNCA foi chamado, já que deu erro antes
        verify(sqsTemplate, never()).send(anyString(), anyString());
    }
}