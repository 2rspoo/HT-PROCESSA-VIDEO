package com.example.order.application.usecases.Order;

import com.example.order.application.ports.out.VideoQueuePort;
import com.example.order.application.ports.out.VideoRepositoryPort;
import com.example.order.application.ports.out.VideoStoragePort;
import com.example.order.domain.entities.Video;
import org.springframework.stereotype.Service;


@Service
public class ProcessVideoUseCase {
    private final VideoRepositoryPort repository;
    private final VideoStoragePort storage;
    private final VideoProcessor processor; // Interface para o FFmpeg

    public void execute(String pedidoId, String fileName) {
        // 1. Inicia Processamento
        repository.updateStatus(pedidoId, "PROCESSING");

        try {
            // 2. Download e Processamento Local
            byte[] videoBytes = storage.download(fileName);
            File zipFile = processor.process(videoBytes, fileName);

            // 3. Upload do Resultado
            String finalUrl = storage.uploadZip(fileName + ".zip", zipFile);

            // 4. Finaliza
            repository.updateUrlAndStatus(pedidoId, finalUrl, "DONE");
        } catch (Exception e) {
            repository.updateStatus(pedidoId, "ERROR");
        }
    }
}