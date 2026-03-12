package com.example.order.infrastructure.adapters.sqs;

import com.example.order.application.ports.out.VideoResultPort;
import com.example.order.domain.entities.VideoMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SqsResultAdapter implements VideoResultPort {

    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;

    // Esta é a URL da NOVA FILA (a fila de respostas)
    @Value("${aws.sqs.results-queue-url}")
    private String resultsQueueUrl;

    public SqsResultAdapter(SqsTemplate sqsTemplate, ObjectMapper objectMapper) {
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendToProcess(VideoMetadata video) { // Assinatura corrigida!
        try {
            // Converte a entidade atualizada para JSON puro
            String jsonPayload = objectMapper.writeValueAsString(video);
            sqsTemplate.send(resultsQueueUrl, jsonPayload);
            System.out.println(">>> Resultado publicado na fila com status: " + video.status());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao publicar resultado do processamento", e);
        }
    }
}