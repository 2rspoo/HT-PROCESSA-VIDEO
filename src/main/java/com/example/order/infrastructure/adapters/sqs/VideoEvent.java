package com.example.order.infrastructure.adapters.sqs;

public record VideoEvent(String PedidoID, String userId, String fileName) {}