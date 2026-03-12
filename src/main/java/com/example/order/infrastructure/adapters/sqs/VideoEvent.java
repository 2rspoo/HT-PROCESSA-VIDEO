package com.example.order.infrastructure.adapters.sqs;

public record VideoEvent(
        String id,
        String userId,
        String fileName,
        String status
) {}