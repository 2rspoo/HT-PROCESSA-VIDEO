package com.example.order.infrastructure.adapters.sqs;

import com.example.order.application.ports.in.ProcessVideoCommand;
import com.example.order.domain.entities.VideoMetadata;
import com.fasterxml.jackson.databind.ObjectMapper; // Import do Jackson
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class SqsVideoListener {

    private final ProcessVideoCommand processVideoCommand;
    private final ObjectMapper objectMapper; // Adicione o ObjectMapper

    public SqsVideoListener(ProcessVideoCommand processVideoCommand, ObjectMapper objectMapper) {
        this.processVideoCommand = processVideoCommand;
        this.objectMapper = objectMapper;
    }

    @SqsListener("${AWS_SQS_URL}")
    public void onMessage(String rawMessage) { // Receba como String pura
        try {
            // Converte manualmente o JSON para o seu VideoEvent local
            VideoEvent event = objectMapper.readValue(rawMessage, VideoEvent.class);

            VideoMetadata domainVideo = new VideoMetadata(
                    event.PedidoID(), // Ajuste conforme os nomes no seu VideoEvent
                    event.userId(),
                    event.fileName(),
                    "RECEIVED",
                    null,
                    LocalDateTime.now()
            );

            processVideoCommand.process(domainVideo);
        } catch (Exception e) {
            System.err.println("Erro ao converter mensagem JSON: " + e.getMessage());
        }
    }
}