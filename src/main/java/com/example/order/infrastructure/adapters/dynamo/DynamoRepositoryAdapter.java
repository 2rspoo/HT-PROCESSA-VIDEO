package com.example.order.infrastructure.adapters.dynamo;

import com.example.order.application.ports.out.VideoRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Map;

@Component // Agora o Spring vai conseguir criar o Bean
public class DynamoRepositoryAdapter implements VideoRepositoryPort { // REMOVIDO O ABSTRACT

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoRepositoryAdapter(DynamoDbClient dynamoDbClient,
                                   @Value("${AWS_DYNAMODB_TABLE:Pedidos}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public void updateStatus(String id, String status) {
        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("PedidoID", AttributeValue.builder().s(id).build()))
                .updateExpression("SET #s = :val")
                .expressionAttributeNames(Map.of("#s", "Status"))
                .expressionAttributeValues(Map.of(":val", AttributeValue.builder().s(status).build()))
                .build();

        dynamoDbClient.updateItem(request);
    }

    // Não esqueça de implementar o outro método da interface também!
    @Override
    public void updateUrlAndStatus(String id, String url, String status) {
        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("PedidoID", AttributeValue.builder().s(id).build()))
                .updateExpression("SET #s = :statusVal, #u = :urlVal")
                .expressionAttributeNames(Map.of("#s", "Status", "#u", "S3Url"))
                .expressionAttributeValues(Map.of(
                        ":statusVal", AttributeValue.builder().s(status).build(),
                        ":urlVal", AttributeValue.builder().s(url).build()
                ))
                .build();

        dynamoDbClient.updateItem(request);
    }
}