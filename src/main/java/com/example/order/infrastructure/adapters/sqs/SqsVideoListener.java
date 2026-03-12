package com.example.order.infrastructure.adapters.sqs;

import com.example.order.application.ports.in.ProcessVideoCommand;
import com.example.order.domain.entities.VideoMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.model.Message; // IMPORTANTE: SDK v2
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
    public void onMessage(Message sqsMessage) { // Recebe a mensagem bruta da SDK
        try {
            // Pegamos o JSON puro do corpo da mensagem
            String jsonBody = sqsMessage.body();
            System.out.println("JSON recebido: " + jsonBody);

            // Convertemos manualmente para o SEU VideoEvent local
            VideoEvent event = objectMapper.readValue(jsonBody, VideoEvent.class);

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
            e.printStackTrace();
        }
    }
}