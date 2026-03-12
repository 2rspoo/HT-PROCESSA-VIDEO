package com.example.order.application.ports.in;

import com.example.order.domain.entities.VideoMetadata;

public interface ProcessVideoCommand {
    /**
     * Inicia o fluxo de processamento do vídeo:
     * Download -> Prints -> Zip -> Upload -> Notificação.
     */

    void process(VideoMetadata videoMetadata);
}