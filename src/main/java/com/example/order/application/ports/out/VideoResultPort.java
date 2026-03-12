package com.example.order.application.ports.out;

import com.example.order.domain.entities.VideoMetadata;

public interface VideoResultPort {
    void sendToProcess(VideoMetadata videometadata);
}
