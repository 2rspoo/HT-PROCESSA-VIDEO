package com.example.order.infrastructure.adapters.sqs;

import com.example.order.application.ports.in.ProcessVideoCommand;
import com.example.order.domain.entities.VideoMetadata;
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
            System.out.println(">>> Processamento iniciado para o ficheiro: " + event.fileName());

            VideoMetadata domainVideo = new VideoMetadata(
                    event.id(),
                    event.userId(),
                    event.fileName(),
                    "RECEIVED",
                    null,
                    LocalDateTime.now()
            );

            processVideoCommand.process(domainVideo);

        } catch (Exception e) {
            System.err.println("Erro durante o processamento do vídeo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}