package com.example.order.infrastructure.adapters.sqs.dto;

public record VideoResultEvent(
        String id,                // O ID do vídeo/pedido original
        String status,            // "COMPLETED" ou "ERROR"
        String processedFileKey,  // Onde o ficheiro processado/zip foi salvo no S3 (ex: "processed/123.zip")
        String errorMessage       // Nulo se deu tudo certo, preenchido se falhou
) {}
