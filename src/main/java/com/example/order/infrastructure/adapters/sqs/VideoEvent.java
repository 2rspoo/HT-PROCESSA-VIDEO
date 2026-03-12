package com.example.order.infrastructure.adapters.sqs;

public record VideoEvent(
        String id,        // Não pode ser pedidoId aqui, o JSON envia "id"
        String userId,
        String fileName
) {}