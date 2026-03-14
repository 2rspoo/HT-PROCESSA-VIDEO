package com.example.order.application.usecases;

import com.example.order.application.ports.in.ProcessVideoCommand;
import com.example.order.application.ports.out.NotificationPort;
import com.example.order.application.ports.out.VideoRepositoryPort;
import com.example.order.application.ports.out.VideoResultPort;
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
    private final VideoResultPort result;
    private final FFmpegVideoProcessor processor; // Seu componente de lógica de vídeo

    public ProcessVideoUseCase(VideoRepositoryPort repository,
                               VideoStoragePort storage,
                               NotificationPort notification,
                               FFmpegVideoProcessor processor,
                               VideoResultPort result) {
        this.repository = repository;
        this.storage = storage;
        this.notification = notification;
        this.processor = processor;
        this.result = result;
    }

    @Override
    public void process(VideoMetadata video) {
        System.out.println("Iniciando processamento do pedido2: " + video.pedidoId());
        try {
            System.out.println("PROCESSING");
            repository.updateStatus(video.pedidoId(), "PROCESSING");

            System.out.println("original do S3");
            byte[] videoData = storage.download(video.pedidoId());

            System.out.println("FFmpeg");
            File zipFile = processor.process(videoData, video.fileName());

            System.out.println(".zip");
            String s3UrlZip = storage.uploadZip(video.pedidoId() + ".zip", zipFile);

            System.out.println("result");
            VideoMetadata videoAtualizado = new VideoMetadata(
                    video.pedidoId(),
                    video.userId(),
                    video.fileName(),
                    "DONE",
                    s3UrlZip,
                    video.createdAt()
            );

            result.sendToProcess(videoAtualizado);

            // --- CORREÇÃO 1: DESCOMENTE A NOTIFICAÇÃO ---
            // O seu teste "shouldProcessVideoSuccessfully" falha porque esta linha estava comentada!


            System.out.println("Processamento concluído com sucesso: " + video.pedidoId());

        } catch (Exception e) {
            System.err.println("Erro ao processar vídeo: " + e.getMessage());

            // --- CORREÇÃO 2: ADICIONE A CHAMADA AO REPOSITÓRIO NO CATCH ---
            // O seu teste "shouldHandleExceptionAndSetStatusToError" falha porque
            // você só envia para a fila (result), mas não chama o repository.updateStatus.
            repository.updateStatus(video.pedidoId(), "ERROR");

            VideoMetadata videoErro = new VideoMetadata(
                    video.pedidoId(),
                    video.userId(),
                    video.fileName(),
                    "ERROR",
                    video.s3Url(),
                    video.createdAt()
            );

            result.sendToProcess(videoErro);
        }
    }
}