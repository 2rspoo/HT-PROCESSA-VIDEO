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
    public void onMessage(software.amazon.awssdk.services.sqs.model.Message sqsMessage) {
        try {
            // Pegamos o corpo da mensagem (Body) que é o JSON puro
            String json = sqsMessage.body();
            System.out.println("JSON recebido da fila: " + json);

            // Convertemos manualmente com o ObjectMapper que você já tem injetado
            VideoEvent event = objectMapper.readValue(json, VideoEvent.class);

            // Converte para sua entidade de domínio e chama o UseCase
            VideoMetadata domainVideo = new VideoMetadata(
                    event.id(),
                    event.userId(),
                    event.fileName(),
                    "RECEIVED",
                    null,
                    java.time.LocalDateTime.now()
            );

            processVideoCommand.process(domainVideo);

        } catch (Exception e) {
            System.err.println("Erro crítico ao processar mensagem SQS: " + e.getMessage());
            e.printStackTrace();
        }
    }
}