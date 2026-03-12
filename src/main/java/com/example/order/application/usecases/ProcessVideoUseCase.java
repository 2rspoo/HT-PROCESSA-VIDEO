package com.example.order.application.usecases;

import com.example.order.application.ports.in.ProcessVideoCommand;
import com.example.order.application.ports.out.NotificationPort;
import com.example.order.application.ports.out.VideoRepositoryPort;
import com.example.order.application.ports.out.VideoStoragePort;

import com.example.order.domain.entities.VideoMetadata;
import com.example.order.infrastructure.adapters.video.FFmpegVideoProcessor;
import org.springframework.stereotype.Service;
import java.io.File;

@Service
public class ProcessVideoUseCase implements ProcessVideoCommand {

    private final VideoRepositoryPort repository;
    private final VideoStoragePort storage;
    private final NotificationPort notification;
    private final FFmpegVideoProcessor processor; // Seu componente de lógica de vídeo

    public ProcessVideoUseCase(VideoRepositoryPort repository,
                               VideoStoragePort storage,
                               NotificationPort notification,
                               FFmpegVideoProcessor processor) {
        this.repository = repository;
        this.storage = storage;
        this.notification = notification;
        this.processor = processor;
    }

    @Override
    public void process(VideoMetadata video) {
        System.out.println("Iniciando processamento do pedido: " + video.pedidoId());

        try {
            // 1. Alterar status no Dynamo para PROCESSING
            repository.updateStatus(video.pedidoId(), "PROCESSING");

            // 2. Ler o vídeo original do S3
            byte[] videoData = storage.download(video.fileName());

            // 3. Processar (FFmpeg: prints + zip)
            File zipFile = processor.process(videoData, video.fileName());

            // 4. Salvar o .zip no S3 e obter a URL
            String s3UrlZip = storage.uploadZip(video.fileName() + ".zip", zipFile);

            // 5. Atualizar Dynamo para DONE com a URL do S3
            repository.updateUrlAndStatus(video.pedidoId(), s3UrlZip, "DONE");

            // 6. Notificar finalização via SQS
            notification.sendNotification(video.pedidoId(), "DONE", s3UrlZip);

            System.out.println("Processamento concluído com sucesso: " + video.pedidoId());

        } catch (Exception e) {
            System.err.println("Erro ao processar vídeo: " + e.getMessage());
            repository.updateStatus(video.pedidoId(), "ERROR");
        }
    }
}