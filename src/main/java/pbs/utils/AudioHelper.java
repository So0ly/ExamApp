package pbs.utils;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.file.Path;

public class AudioHelper {

    private static final Logger LOGGER = org.jboss.logging.Logger.getLogger(AudioHelper.class);

    public static String getTranscriptFromAudio(String audioPath) {
        LOGGER.debugf("Transcribing audio: {}", audioPath);
        Path audioSource = Path.of(ConfigProvider.getConfig().getValue("AUDIO_STORAGE_PATH", String.class));
        Path audioFile = audioSource.resolve(audioPath);
        String modelPath = ConfigProvider.getConfig().getValue("vosk.model.path", String.class);
        LibVosk.setLogLevel(LogLevel.DEBUG);
        try (Model model = new Model(modelPath);
             AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile.toFile());
             Recognizer recognizer = new Recognizer(model, 16000)) {

            int nbytes;
            byte[] b = new byte[4096];
            while ((nbytes = ais.read(b)) >= 0) {
                if (recognizer.acceptWaveForm(b, nbytes)) {
                    System.out.println(recognizer.getResult());
                } else {
                    System.out.println(recognizer.getPartialResult());
                }
            }

            System.out.println(recognizer.getFinalResult());
            return recognizer.getFinalResult();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
