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
            // 1. Alterar status no Dynamo para PROCESSING
            System.out.println("PROCESSING " );
            repository.updateStatus(video.pedidoId(), "PROCESSING");

            // 2. Ler o vídeo original do S3
            System.out.println("original do S3 " );

            byte[] videoData = storage.download(video.pedidoId());

            // 3. Processar (FFmpeg: prints + zip)
            System.out.println("FFmpeg " );
            File zipFile = processor.process(videoData, video.fileName());

            // 4. Salvar o .zip no S3 e obter a URL
            System.out.println(".zip" );
            String s3UrlZip = storage.uploadZip(video.pedidoId() + ".zip", zipFile);

            // 5. Atualizar Dynamo para DONE com a URL do S3
            System.out.println("result" );
// Supondo que você tem o objeto 'video' original
            String novoStatus = "DONE"; // ou "PROCESSING"
            String novaS3Url = s3UrlZip;

// Cria a cópia atualizada
            VideoMetadata videoAtualizado = new VideoMetadata(
                    video.pedidoId(),       // Mantém o antigo
                    video.userId(),         // Mantém o antigo
                    video.fileName(),       // Mantém o antigo
                    novoStatus,             // NOVO VALOR!
                    novaS3Url,              // NOVO VALOR!
                    video.createdAt()       // Mantém o antigo
            );

// Envia o NOVO objeto para a fila
            result.sendToProcess(videoAtualizado);

            // repository.updateUrlAndStatus(video.pedidoId(), s3UrlZip, "DONE");

            // 6. Notificar finalização via SQS
            System.out.println("Notificar" );
            notification.sendNotification(video.pedidoId(), "DONE", s3UrlZip);


            System.out.println("Processamento concluído com sucesso: " + video.pedidoId());

        } catch (Exception e) {
            System.err.println("Erro ao processar vídeo: " + e.getMessage());
            String novoStatus = "ERRO"; // ou "PROCESSING"


// Cria a cópia atualizada
            VideoMetadata videoAtualizado = new VideoMetadata(
                    video.pedidoId(),       // Mantém o antigo
                    video.userId(),         // Mantém o antigo
                    video.fileName(),       // Mantém o antigo
                    novoStatus,             // NOVO VALOR!
                    video.s3Url(),                     // NOVO VALOR!
                    video.createdAt()       // Mantém o antigo
            );

// Envia o NOVO objeto para a fila
            result.sendToProcess(videoAtualizado);
        }
    }
}