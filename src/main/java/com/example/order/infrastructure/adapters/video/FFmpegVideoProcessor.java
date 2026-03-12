package com.example.order.infrastructure.adapters.video;

import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class FFmpegVideoProcessor {

    public File process(byte[] videoData, String fileName) throws Exception {
        // 1. Criar diretório temporário para o trabalho atual
        System.out.println("1" );
        Path workDir = Files.createTempDirectory("video_proc_" + System.currentTimeMillis());
        Path framesDir = Files.createDirectory(workDir.resolve("frames"));
        System.out.println("2" );
        // 2. Salvar o array de bytes em um arquivo físico para o FFmpeg ler
        Path videoInputPath = workDir.resolve(fileName);
        Files.write(videoInputPath, videoData);
        System.out.println("3" );
        // 3. Executar o FFmpeg
        // Comando: ffmpeg -i video.mp4 -vf "fps=1/2" frames/img%03d.jpg
        // "fps=1/2" significa 1 frame a cada 2 segundos.
        executeFFmpegCommand(videoInputPath, framesDir);
        System.out.println("4" );
        // 4. Compactar os frames gerados em um ZIP
        File zipFile = createZipFromFolder(framesDir, workDir.resolve("processed_frames.zip"));
        System.out.println("5" );
        // 5. Limpeza: Você pode deletar o diretório temporário após o upload no UseCase
        // (Por enquanto, retornamos o arquivo ZIP para ser enviado ao S3)
        System.out.println("6" );
        return zipFile;
    }

    private void executeFFmpegCommand(Path videoInput, Path outputDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", videoInput.toString(),
                "-vf", "fps=1/2",
                outputDir.toString() + "/frame-%03d.jpg"
        );

        // Redireciona erros para o log do Java para facilitar o debug
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Opcional: Logar a saída do FFmpeg em caso de erro
                // System.out.println("FFmpeg: " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg falhou com código de saída: " + exitCode);
        }
    }

    private File createZipFromFolder(Path sourceDir, Path zipPath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            Files.walk(sourceDir)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(path.getFileName().toString());
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
        return zipPath.toFile();
    }
}