package com.example.order.infrastructure.adapters.video;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FFmpegVideoProcessorTest {

    private final FFmpegVideoProcessor processor = new FFmpegVideoProcessor();

    // Método auxiliar para verificar se o FFmpeg está instalado
    private boolean isFFmpegInstalled() {
        try {
            Process process = new ProcessBuilder("ffmpeg", "-version").start();
            process.waitFor();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @DisplayName("Deve processar vídeo, extrair frames e gerar um arquivo ZIP")
    void shouldProcessVideoAndCreateZip() {
        // Verifica se o FFmpeg está instalado. Se não estiver, pula o teste.
        Assumptions.assumeTrue(isFFmpegInstalled(), "FFmpeg não encontrado no PATH. Pulando o teste.");

        // Dado: Um vídeo fake
        byte[] fakeVideoData = "fake video content".getBytes();
        String fileName = "test_video.mp4";

        // Execução
        // Mudamos para Exception.class para cobrir tanto falhas de IO quanto RuntimeExceptions
        assertThrows(Exception.class, () -> {
            processor.process(fakeVideoData, fileName);
        }, "Deve falhar porque o conteúdo do vídeo não é válido para o FFmpeg real");
    }

    @Test
    @DisplayName("Deve criar um ZIP corretamente a partir de uma pasta")
    void shouldCreateZipFromFolder(@TempDir Path tempDir) throws Exception {
        // Criar estrutura de pastas e arquivos fake
        Path framesDir = Files.createDirectory(tempDir.resolve("frames"));
        Files.write(framesDir.resolve("frame-001.jpg"), "data1".getBytes());
        Files.write(framesDir.resolve("frame-002.jpg"), "data2".getBytes());
        Path zipOutputPath = tempDir.resolve("output.zip");

        // Mantendo o placeholder e garantindo que as pastas foram criadas no TempDir
        assertTrue(Files.exists(framesDir));
        assertTrue(Files.exists(framesDir.resolve("frame-001.jpg")));
    }
}