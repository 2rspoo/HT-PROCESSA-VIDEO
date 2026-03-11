package com.example.order.infrastructure.adapters.sqs;

public record VideoEvent(
        String pedidoId,
        String fileName,
        String userId
) {}