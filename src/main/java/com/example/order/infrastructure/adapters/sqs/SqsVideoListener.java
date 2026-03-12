package com.example.order.infrastructure.adapters.sqs;

import com.example.order.application.ports.in.ProcessVideoCommand;
import com.example.order.domain.entities.VideoMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.messaging.Message; // IMPORTANTE: Use este import
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class SqsVideoListener {

    private final ProcessVideoCommand processVideoCommand;
    private final ObjectMapper objectMapper;

    public SqsVideoListener(ProcessVideoCommand processVideoCommand, ObjectMapper objectMapper) {
        this.processVideoCommand = processVideoCommand;
        this.objectMapper = objectMapper;
    }

    @SqsListener("${AWS_SQS_URL}")
    public void onMessage(Message<String> message) { // Recebe como Message de String
        try {
            // O payload é o JSON puro em formato String
            String json = message.getPayload();
            System.out.println(">>> JSON recebido: " + json);

            VideoEvent event = objectMapper.readValue(json, VideoEvent.class);

            VideoMetadata domainVideo = new VideoMetadata(
                    event.id(),
                    event.userId(),
                    event.fileName(),
                    "RECEIVED",
                    null,
                    LocalDateTime.now()
            );

            processVideoCommand.process(domainVideo);

        } catch (Exception e) {
            System.err.println("Erro ao processar mensagem: " + e.getMessage());
        }
    }
}