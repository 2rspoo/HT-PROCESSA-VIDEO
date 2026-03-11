package com.example.order.application.ports.out;

import com.example.order.domain.entities.VideoMetadata;

// application/ports/output/VideoRepositoryPort.java
public interface VideoRepositoryPort {
    void updateStatus(String id, String status);
    void updateUrlAndStatus(String id, String url, String status);
}