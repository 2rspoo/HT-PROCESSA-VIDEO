package com.example.order.infrastructure.adapters.video;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FFmpegVideoProcessorTest {

    private final FFmpegVideoProcessor processor = new FFmpegVideoProcessor();

    @Test
    @DisplayName("Deve processar o vídeo, simular o FFmpeg e gerar um arquivo ZIP")
    void shouldProcessVideoSuccessfully() throws Exception {
        byte[] dummyVideoData = "dados-fake-do-video".getBytes();
        String fileName = "video-teste.mp4";

        // Intercepta a criação do ProcessBuilder para não rodar o FFmpeg de verdade
        try (MockedConstruction<ProcessBuilder> mocked = Mockito.mockConstruction(ProcessBuilder.class,
                (mockBuilder, context) -> {
                    Process mockProcess = mock(Process.class);

                    // Simula que o comando do terminal rodou com sucesso (código 0)
                    when(mockProcess.waitFor()).thenReturn(0);

                    // Simula o log de saída vazio para o BufferedReader não dar NullPointer
                    InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
                    when(mockProcess.getInputStream()).thenReturn(emptyStream);

                    when(mockBuilder.start()).thenReturn(mockProcess);
                    when(mockBuilder.redirectErrorStream(true)).thenReturn(mockBuilder);
                })) {

            // Execução
            File resultZip = processor.process(dummyVideoData, fileName);

            // Verificações
            assertNotNull(resultZip);
            assertTrue(resultZip.exists());
            assertTrue(resultZip.getName().endsWith(".zip"));

            // Limpeza do arquivo gerado no teste
            resultZip.deleteOnExit();
        }
    }

    @Test
    @DisplayName("Deve lançar exceção quando o FFmpeg retornar erro")
    void shouldThrowExceptionWhenFFmpegFails() throws Exception {
        byte[] dummyVideoData = "dados-fake".getBytes();
        String fileName = "video-erro.mp4";

        try (MockedConstruction<ProcessBuilder> mocked = Mockito.mockConstruction(ProcessBuilder.class,
                (mockBuilder, context) -> {
                    Process mockProcess = mock(Process.class);

                    // Simula erro no FFmpeg (código 1 em vez de 0)
                    when(mockProcess.waitFor()).thenReturn(1);

                    InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
                    when(mockProcess.getInputStream()).thenReturn(emptyStream);

                    when(mockBuilder.start()).thenReturn(mockProcess);
                    when(mockBuilder.redirectErrorStream(true)).thenReturn(mockBuilder);
                })) {

            // Execução e Verificação
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                processor.process(dummyVideoData, fileName);
            });

            // Valida se a mensagem de erro é a esperada
            assertTrue(exception.getMessage().contains("código de saída: 1"));
        }
    }
}