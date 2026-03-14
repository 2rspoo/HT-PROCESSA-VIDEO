package com.example.order.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VideoMetadataTest {

    @Test
    @DisplayName("Deve retornar true quando o nome do arquivo terminar com .mp4 minúsculo")
    void canBeProcessed_ShouldReturnTrue_WhenFileNameEndsWithMp4() {
        // Arrange
        VideoMetadata metadata = new VideoMetadata(
                "pedido-123", "user-1", "meu_video.mp4", "PENDING", "s3://bucket/video.mp4", LocalDateTime.now()
        );

        // Act & Assert
        assertTrue(metadata.canBeProcessed());
    }

    @Test
    @DisplayName("Deve retornar false quando o nome do arquivo for null")
    void canBeProcessed_ShouldReturnFalse_WhenFileNameIsNull() {
        // Arrange
        VideoMetadata metadata = new VideoMetadata(
                "pedido-123", "user-1", null, "PENDING", null, LocalDateTime.now()
        );

        // Act & Assert
        assertFalse(metadata.canBeProcessed());
    }

    @Test
    @DisplayName("Deve retornar false quando o nome do arquivo tiver outra extensão")
    void canBeProcessed_ShouldReturnFalse_WhenFileNameHasOtherExtension() {
        // Arrange
        VideoMetadata metadata = new VideoMetadata(
                "pedido-123", "user-1", "meu_video.avi", "PENDING", "s3://bucket/video.avi", LocalDateTime.now()
        );

        // Act & Assert
        assertFalse(metadata.canBeProcessed());
    }

    @Test
    @DisplayName("Deve manter os valores corretamente ao instanciar o record")
    void shouldRetainValuesProperly() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        VideoMetadata metadata = new VideoMetadata(
                "p1", "u1", "video.mp4", "UPLOADED", "url", now
        );

        // Act & Assert
        assertEquals("p1", metadata.pedidoId());
        assertEquals("u1", metadata.userId());
        assertEquals("video.mp4", metadata.fileName());
        assertEquals("UPLOADED", metadata.status());
        assertEquals("url", metadata.s3Url());
        assertEquals(now, metadata.createdAt());
    }
}