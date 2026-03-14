package com.example.order.infrastructure.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

import static org.assertj.core.api.Assertions.assertThat;

class AwsConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AwsConfig.class)
            .withPropertyValues(
                    "AWS_REGION=us-east-1",
                    "AWS_ACCESS_KEY_ID=fake-key",
                    "AWS_SECRET_ACCESS_KEY=fake-secret"
            );

    @Test
    @DisplayName("Deve carregar todos os Beans da AWS no contexto do Spring")
    void shouldLoadAwsBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(S3Client.class);
            assertThat(context).hasSingleBean(DynamoDbClient.class);
            assertThat(context).hasSingleBean(SqsClient.class);
        });
    }
}