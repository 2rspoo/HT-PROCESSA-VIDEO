package com.example.order.infrastructure.adapters.sqs;

import com.example.order.application.ports.out.NotificationPort; // Import da interface acima
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component // OBRIGATÓRIO: Sem isso o Spring não "vê" esta classe
public class SqsNotificationAdapter implements NotificationPort {

    private final SqsClient sqsClient;
    private final String queueUrl;

    public SqsNotificationAdapter(SqsClient sqsClient,
                                  @Value("${AWS_SQS_QUEUE_URL}") String queueUrl) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
    }

    @Override
    public void sendNotification(String pedidoId, String status, String s3Url) {
        // Lógica de envio da mensagem para a fila SQS
        String messageBody = String.format(
                "{\"pedidoId\":\"%s\", \"status\":\"%s\", \"s3Url\":\"%s\"}",
                pedidoId, status, s3Url
        );

        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .build());
    }
}