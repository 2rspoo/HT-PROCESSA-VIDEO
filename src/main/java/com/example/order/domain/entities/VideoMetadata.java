package com.example.order.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

// domain/entities/VideoMetadata.java

import java.time.LocalDateTime;

public record VideoMetadata(
        String pedidoId,
        String userId,
        String fileName,
        String status,
        String s3Url,
        LocalDateTime createdAt
) {
    // Você pode adicionar métodos de validação de negócio aqui se precisar
    public boolean canBeProcessed() {
        return fileName != null && fileName.endsWith(".mp4");
    }
}