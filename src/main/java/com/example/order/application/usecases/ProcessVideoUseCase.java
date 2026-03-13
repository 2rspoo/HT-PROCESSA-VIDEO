package com.example.order.application.usecases;

import com.example.order.application.ports.in.ProcessVideoCommand;
import com.example.order.application.ports.out.NotificationPort;
import com.example.order.application.ports.out.VideoRepositoryPort;
import com.example.order.application.ports.out.VideoResultPort;
import com.example.order.application.ports.out.VideoStoragePort;
import com.example.order.domain.entities.VideoMetadata;
import com.example.order.infrastructure.adapters.video.FFmpegVideoProcessor;
import io.micrometer.core.instrument.Counter; // Import correto
import io.micrometer.core.instrument.MeterRegistry; // Import correto
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ProcessVideoUseCase implements ProcessVideoCommand {

    // Métricas
    private final Counter successCounter;
    private final Counter errorCounter;

    // Dependências
    private final VideoRepositoryPort repository;
    private final VideoStoragePort storage;
    private final NotificationPort notification;
    private final VideoResultPort result;
    private final FFmpegVideoProcessor processor;

    public ProcessVideoUseCase(VideoRepositoryPort repository,
                               VideoStoragePort storage,
                               NotificationPort notification,
                               FFmpegVideoProcessor processor,
                               VideoResultPort result,
                               MeterRegistry registry) { // Recebe o registry do Spring
        this.repository = repository;
        this.storage = storage;
        this.notification = notification;
        this.processor = processor;
        this.result = result;

        // Inicialização dos contadores (seguindo o padrão do seu Guia de Monitoramento)
        this.successCounter = Counter.builder("video_processing_total")
                .tag("status", "success")
                .description("Total de vídeos processados com sucesso")
                .register(registry);

        this.errorCounter = Counter.builder("video_processing_total")
                .tag("status", "error")
                .description("Total de vídeos que falharam no processamento")
                .register(registry);
    }

    @Override
    public void process(VideoMetadata video) {
        System.out.println("Iniciando processamento do pedido: " + video.pedidoId());
        try {
            // 1. Alterar status no Dynamo para PROCESSING
            repository.updateStatus(video.pedidoId(), "PROCESSING");

            // 2. Ler o vídeo original do S3
            byte[] videoData = storage.download(video.pedidoId());

            // 3. Processar (FFmpeg)
            File zipFile = processor.process(videoData, video.fileName());

            // 4. Salvar o .zip no S3 e obter a URL
            String s3UrlZip = storage.uploadZip(video.pedidoId() + ".zip", zipFile);

            // 5. Atualizar Resultado
            VideoMetadata videoAtualizado = new VideoMetadata(
                    video.pedidoId(),
                    video.userId(),
                    video.fileName(),
                    "DONE",
                    s3UrlZip,
                    video.createdAt()
            );

            result.sendToProcess(videoAtualizado);

            // 6. Notificar finalização via SQS
            notification.sendNotification(video.pedidoId(), "DONE", s3UrlZip);

            // --- INSTRUMENTAÇÃO: SUCESSO ---
            successCounter.increment();
            System.out.println("Processamento concluído com sucesso: " + video.pedidoId());

        } catch (Exception e) {
            // --- INSTRUMENTAÇÃO: ERRO ---
            errorCounter.increment();

            System.err.println("Erro ao processar vídeo: " + e.getMessage());
            repository.updateStatus(video.pedidoId(), "ERROR");
            // Dependendo da sua lógica, você pode relançar a exceção ou apenas logar
        }
    }
}