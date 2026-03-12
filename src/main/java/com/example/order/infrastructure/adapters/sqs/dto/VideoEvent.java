package com.example.order.infrastructure.adapters.sqs.dto;

public record VideoEvent(
        String id,
        String userId,
        String fileName
) {}