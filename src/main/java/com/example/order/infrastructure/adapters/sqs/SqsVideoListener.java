package com.example.order.infrastructure.adapters.sqs;

import com.example.order.application.ports.out.VideoQueuePort;
import com.example.order.domain.entities.Video;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class SqsVideoListener {
    private final ProcessVideoUseCase useCase;

    @SqsListener("${AWS_SQS_QUEUE_URL}")
    public void receiveMessage(@Payload String messageJson) {
        // Use Jackson para converter o JSON da mensagem
        // Exemplo: {"pedidoId": "123", "fileName": "video.mp4"}
        VideoEvent event = objectMapper.readValue(messageJson, VideoEvent.class);
        useCase.execute(event.getPedidoId(), event.getFileName());
    }
}