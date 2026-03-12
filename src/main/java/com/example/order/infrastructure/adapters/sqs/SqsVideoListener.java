package com.example.order.infrastructure.adapters.sqs;

import com.example.order.application.ports.in.ProcessVideoCommand;
import com.example.order.domain.entities.VideoMetadata;
import com.example.order.infrastructure.adapters.sqs.dto.VideoEvent;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class SqsVideoListener {

    private final ProcessVideoCommand processVideoCommand;

    public SqsVideoListener(ProcessVideoCommand processVideoCommand) {
        this.processVideoCommand = processVideoCommand;
    }

    // O Spring converte automaticamente porque o JSON agora é puro!
    @SqsListener("${AWS_SQS_URL}")
    public void onMessage(VideoEvent event) {
        try {
            // Monta a key exata que foi usada no S3 durante o upload
            String s3Key = "uploads/" + event.id();

            System.out.println(">>> Processamento iniciado para o ficheiro S3: " + s3Key);

            VideoMetadata domainVideo = new VideoMetadata(
                    event.id(),
                    event.userId(),
                    event.fileName(), // Passa a S3 Key montada para o adaptador conseguir fazer o download
                    "RECEIVED",
                    null,
                    LocalDateTime.now()
            );
            System.out.println(">>> Chama process " + s3Key);
            processVideoCommand.process(domainVideo);

        } catch (Exception e) {
            System.err.println("Erro durante o processamento do vídeo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}