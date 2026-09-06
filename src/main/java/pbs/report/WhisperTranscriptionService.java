package pbs.report;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class WhisperTranscriptionService {

    private static final Logger LOG = Logger.getLogger(WhisperTranscriptionService.class);

    @ConfigProperty(name = "whisper.executable", defaultValue = "python")
    String whisperExecutable;

    @ConfigProperty(name = "whisper.module", defaultValue = "whisper")
    String whisperModule;

    @ConfigProperty(name = "whisper.model", defaultValue = "medium")
    String whisperModel;

    @ConfigProperty(name = "whisper.language", defaultValue = "pl")
    String whisperLanguage;

    @ConfigProperty(name = "whisper.timeout", defaultValue = "PT2H")
    Duration whisperTimeout;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Instance<ReportService> reportService;

    public WhisperTranscriptionService(Instance<ReportService> reportService) {
        this.reportService = reportService;
    }

    public void transcribeInBackground(Long reportId, Path audioPath) {
        executor.submit(() -> {
            try {
                LOG.infof("Starting Whisper transcription for report %d", reportId);
                String transcription = transcribe(audioPath);
                reportService.get().saveTranscription(reportId, transcription).await().indefinitely();
                LOG.infof("Whisper transcription saved for report %d", reportId);
            } catch (Exception e) {
                LOG.errorf(e, "Whisper transcription failed for report %d", reportId);
            }
        });
    }

    private String transcribe(Path audioPath) throws IOException, InterruptedException {
        Path outputDirectory = Files.createTempDirectory("whisper-");
        try {
            Path stdoutFile = outputDirectory.resolve("whisper.stdout.log");
            Path stderrFile = outputDirectory.resolve("whisper.stderr.log");
            List<String> command = List.of(
                    whisperExecutable,
                    "-m", whisperModule,
                    audioPath.toString(),
                    "--model", whisperModel,
                    "--language", whisperLanguage,
                    "--task", "transcribe",
                    "--output_format", "txt",
                    "--output_dir", outputDirectory.toString()
            );
            Process process;
            try {
                process = new ProcessBuilder(command)
                        .redirectError(stderrFile.toFile())
                        .redirectOutput(stdoutFile.toFile())
                        .start();
            } catch (IOException e) {
                throw new IOException(
                        "Could not start Whisper. Configure whisper.executable to a Python executable " +
                                "with openai-whisper installed, for example C:\\Python314\\python.exe",
                        e
                );
            }
            if (!process.waitFor(whisperTimeout.toSeconds(), TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IOException("Whisper process timed out");
            }
            if (process.exitValue() != 0) {
                throw new IOException("Whisper process exited with code " + process.exitValue() +
                        ": " + readLog(stderrFile));
            }

            List<Path> transcriptFiles;
            try (var files = Files.walk(outputDirectory)) {
                transcriptFiles = files
                        .filter(path -> path.toString().toLowerCase().endsWith(".txt"))
                        .filter(path -> !path.equals(stdoutFile) && !path.equals(stderrFile))
                        .toList();
            }
            if (transcriptFiles.isEmpty()) {
                throw new IOException("Whisper produced no transcript file. stdout: " +
                        readLog(stdoutFile) + "; stderr: " + readLog(stderrFile));
            }
            return Files.readString(transcriptFiles.getFirst(), StandardCharsets.UTF_8).trim();
        } finally {
            try (var files = Files.walk(outputDirectory)) {
                files.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        LOG.warnf(e, "Could not remove temporary Whisper file %s", path);
                    }
                });
            }
        }
    }

    private String readLog(Path logFile) {
        try {
            return Files.readString(logFile, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            return "unavailable";
        }
    }


    @PreDestroy
    void shutdown() {
        executor.shutdown();
    }
}
