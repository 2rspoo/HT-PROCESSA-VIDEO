package com.example.order.infrastructure.adapters.dynamo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DynamoRepositoryAdapterTest {

    @Mock
    private DynamoDbClient dynamoDbClient;

    private DynamoRepositoryAdapter adapter;
    private final String tableName = "TestTable";

    @BeforeEach
    void setUp() {
        adapter = new DynamoRepositoryAdapter(dynamoDbClient, tableName);
    }

    @Test
    @DisplayName("Deve montar o request de updateStatus corretamente")
    void shouldUpdateStatusCorrectly() {
        String id = "123";
        String status = "PROCESSING";

        // Execução
        adapter.updateStatus(id, status);

        // Captura o request enviado para o DynamoDB
        ArgumentCaptor<UpdateItemRequest> captor = ArgumentCaptor.forClass(UpdateItemRequest.class);
        verify(dynamoDbClient).updateItem(captor.capture());

        UpdateItemRequest capturedRequest = captor.getValue();

        // Validações
        assertEquals(tableName, capturedRequest.tableName());
        assertEquals(id, capturedRequest.key().get("PedidoID").s());
        assertEquals(status, capturedRequest.expressionAttributeValues().get(":val").s());
        assertEquals("SET #s = :val", capturedRequest.updateExpression());
    }

    @Test
    @DisplayName("Deve montar o request de updateUrlAndStatus corretamente")
    void shouldUpdateUrlAndStatusCorrectly() {
        String id = "456";
        String url = "https://s3.aws.com/video.zip";
        String status = "COMPLETED";

        // Execução
        adapter.updateUrlAndStatus(id, url, status);

        // Captura o request
        ArgumentCaptor<UpdateItemRequest> captor = ArgumentCaptor.forClass(UpdateItemRequest.class);
        verify(dynamoDbClient).updateItem(captor.capture());

        UpdateItemRequest capturedRequest = captor.getValue();

        // Validações
        assertEquals(id, capturedRequest.key().get("PedidoID").s());
        assertEquals(status, capturedRequest.expressionAttributeValues().get(":statusVal").s());
        assertEquals(url, capturedRequest.expressionAttributeValues().get(":urlVal").s());
        assertEquals("SET #s = :statusVal, #u = :urlVal", capturedRequest.updateExpression());
    }
}