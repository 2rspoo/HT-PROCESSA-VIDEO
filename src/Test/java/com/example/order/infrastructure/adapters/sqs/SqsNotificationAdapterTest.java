package com.example.order.infrastructure.adapters.sqs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SqsNotificationAdapterTest {

    @Mock
    private SqsClient sqsClient;

    private SqsNotificationAdapter adapter;
    private final String queueUrl = "https://sqs.us-east-1.amazonaws.com/123456789/TestQueue";

    @BeforeEach
    void setUp() {
        adapter = new SqsNotificationAdapter(sqsClient, queueUrl);
    }

    @Test
    @DisplayName("Deve montar e enviar a mensagem JSON corretamente para o SQS")
    void shouldSendNotificationSuccessfully() {
        // Dados de entrada
        String pedidoId = "ped-999";
        String status = "FINISHED";
        String s3Url = "http://s3.com/video.zip";

        // Execução
        adapter.sendNotification(pedidoId, status, s3Url);

        // Captura o request enviado para o SQS
        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest capturedRequest = captor.getValue();

        // Validações
        assertEquals(queueUrl, capturedRequest.queueUrl());

        String body = capturedRequest.messageBody();
        assertTrue(body.contains("\"pedidoId\":\"ped-999\""));
        assertTrue(body.contains("\"status\":\"FINISHED\""));
        assertTrue(body.contains("\"s3Url\":\"http://s3.com/video.zip\""));
    }
}