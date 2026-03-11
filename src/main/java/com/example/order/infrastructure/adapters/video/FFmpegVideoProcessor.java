@Component
public class FFmpegVideoProcessor {

    public File process(byte[] videoData, String fileName) throws Exception {
        Path tempDir = Files.createTempDirectory("frames");
        File videoFile = new File(tempDir.toFile(), fileName);
        Files.write(videoFile.toPath(), videoData);

        // Executa FFmpeg: print a cada 2 segundos
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", videoFile.getAbsolutePath(),
                "-vf", "fps=1/2", tempDir.toString() + "/img%03d.jpg"
        );
        pb.start().waitFor();

        return zipFolder(tempDir);
    }

    private File zipFolder(Path sourceDirPath) throws IOException {
        Path zipPath = Files.createTempFile("processed-", ".zip");
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            Files.walk(sourceDirPath)
                    .filter(path -> !Files.isDirectory(path) && path.toString().endsWith(".jpg"))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) { e.printStackTrace(); }
                    });
        }
        return zipPath.toFile();
    }
}