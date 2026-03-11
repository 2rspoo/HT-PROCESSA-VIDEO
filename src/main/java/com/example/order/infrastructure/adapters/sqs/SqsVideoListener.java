package com.example.order.infrastructure.adapters.sqs;

import com.example.order.application.ports.in.ProcessVideoCommand;
import com.example.order.domain.entities.VideoMetadata;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class SqsVideoListener {

    private final ProcessVideoCommand processVideoCommand;

    public SqsVideoListener(ProcessVideoCommand processVideoCommand) {
        this.processVideoCommand = processVideoCommand;
    }

    @SqsListener("${AWS_SQS_QUEUE_URL}")
    public void onMessage(VideoEvent event) {
        // Converte o DTO da infraestrutura para a Entidade de Domínio
        VideoMetadata domainVideo = new VideoMetadata(
                event.pedidoId(),
                event.userId(),
                event.fileName(),
                "RECEIVED",
                null,
                LocalDateTime.now()
        );

        // Executa o comando
        processVideoCommand.process(domainVideo);
    }
}