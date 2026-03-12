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
    public void onMessage(String rawBody) { // Recebe o JSON como texto puro
        try {
            // O ObjectMapper (Jackson) converte o texto no seu DTO local
            // Ignorando qualquer cabeçalho "JavaType" que o outro serviço enviou
            VideoEvent event = objectMapper.readValue(rawBody, VideoEvent.class);

            // Converte para o Domínio
            VideoMetadata domainVideo = new VideoMetadata(
                    event.PedidoID(),
                    event.userId(),
                    event.fileName(),
                    "RECEIVED",
                    null,
                    LocalDateTime.now()
            );

            processVideoCommand.process(domainVideo);
        } catch (Exception e) {
            System.err.println("Erro crítico na conversão do JSON: " + e.getMessage());
        }
    }
}