package com.example.order.infrastructure.adapters.dynamo;

import com.example.order.application.ports.out.VideoRepositoryPort;
import com.example.order.domain.entities.Video;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

@Component
public abstract class DynamoRepositoryAdapter implements VideoRepositoryPort {

    private final DynamoDbClient dynamoDbClient; // Final garante que será inicializada
    private final String tableName;

    // O Spring vê este construtor e procura o Bean 'dynamoDbClient' que criamos acima
    public DynamoRepositoryAdapter(DynamoDbClient dynamoDbClient,
                                   @Value("${AWS_DYNAMODB_TABLE:Pedidos}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public void updateStatus(String id, String status) {
        // Agora o dynamoDbClient não estará mais nulo aqui
        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("PedidoID", AttributeValue.builder().s(id).build()))
                .updateExpression("SET #s = :val")
                .expressionAttributeNames(Map.of("#s", "Status"))
                .expressionAttributeValues(Map.of(":val", AttributeValue.builder().s(status).build()))
                .build();

        dynamoDbClient.updateItem(request);
    }
}