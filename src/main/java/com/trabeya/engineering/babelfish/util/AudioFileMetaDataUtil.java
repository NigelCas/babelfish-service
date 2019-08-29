package com.trabeya.engineering.babelfish.util;

import com.trabeya.engineering.babelfish.exceptions.AudioFileMetaDataException;
import com.trabeya.engineering.babelfish.model.AudioFileMetaData;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
public class AudioFileMetaDataUtil {

    AudioFileMetaDataUtil(){}

    private static String detectDocTypeUsingFacade(File stream)
            throws IOException {
        Tika tika = new Tika();
        return tika.detect(stream);
    }

    public static AudioFileMetaData listAudioMetaDataFromBytes(byte[] inputFileData) {
        AudioFileMetaData modelMetaData = new AudioFileMetaData();
        FileOutputStream fos = null;
        try {
            File tempFile = File.createTempFile("tmp", null, null);
            fos = new FileOutputStream(tempFile);
            fos.write(inputFileData);

            // run audio detection
            String detectedMime = detectDocTypeUsingFacade(tempFile);
            log.info("Apache Tika Audio MimeDetection result : "+detectedMime);
            switch (detectedMime){
                case "audio/mpeg":
                    File tempFileMp3 = File.createTempFile("tmp", ".mp3", null);
                    Files.move(tempFile.toPath(), tempFileMp3.toPath(), REPLACE_EXISTING);
                    getMetaDataFromMP3File(tempFileMp3, modelMetaData);
                    break;
                case "audio/flac":
                    File tempFileFlac = File.createTempFile("tmp", ".flac", null);
                    Files.move(tempFile.toPath(), tempFileFlac.toPath(), REPLACE_EXISTING);
                    getMetaDataFromAudioFile(tempFileFlac, modelMetaData);
                    break;
                case "audio/ogg":
                case "audio/opus":
                    File tempFileOpus = File.createTempFile("tmp", ".opus", null);
                    Files.move(tempFile.toPath(), tempFileOpus.toPath(), REPLACE_EXISTING);
                    getMetaDataFromAudioFile(tempFileOpus, modelMetaData);
                    break;
                case "audio/speex":
                    File tempFileSpeex = File.createTempFile("tmp", ".speex", null);
                    Files.move(tempFile.toPath(), tempFileSpeex.toPath(), REPLACE_EXISTING);
                    getMetaDataFromAudioFile(tempFileSpeex, modelMetaData);
                    break;
                case "audio/wave":
                case "audio/wav":
                case "audio/vnd.wave":
                    File tempFileWav = File.createTempFile("tmp", ".wav", null);
                    Files.move(tempFile.toPath(), tempFileWav.toPath(), REPLACE_EXISTING);
                    getMetaDataFromAudioFile(tempFileWav, modelMetaData);
                    break;
                default : log.warn("Unsupported File Extension: "+detectedMime+", No metadata extracted!" ); break;
            }

        } catch (Exception ex) {
            log.error("listMetaDataFromBytes error : ", ex);
            throw new AudioFileMetaDataException(ex.getMessage());
        }
        finally {
            try {
                assert fos != null;
                fos.close();
            } catch (Exception e) {
                log.error("listMetaDataFromBytes FileOutputStream closing error : ", e);
            }
        }
        return modelMetaData;
    }

    private static void getMetaDataFromMP3File(File file, AudioFileMetaData modelMetaData) {
        try {
            MP3File mp3File = (MP3File) AudioFileIO.read(file);
            MP3AudioHeader audioHeader = mp3File.getMP3AudioHeader();
            LocalTime time = LocalTime.parse("00:00:00", DateTimeFormatter.ISO_TIME);
            time = time.plusSeconds(Double.valueOf(audioHeader.getPreciseTrackLength()).longValue());

            modelMetaData.setTrackLengthIso(time);
            modelMetaData.setContentType(audioHeader.getEncodingType());
            modelMetaData.setChannels(audioHeader.getChannels());
            modelMetaData.setSampleRateHz(audioHeader.getSampleRate());
            modelMetaData.setBitRateKbps(audioHeader.getBitRate());

        } catch (CannotReadException | ReadOnlyFileException | TagException
                | IOException | InvalidAudioFrameException e) {
            log.error("getMetaDataFromMP3File error : ", e);
        }
    }

    private static void getMetaDataFromAudioFile(File file, AudioFileMetaData modelMetaData) {
        try {
            AudioFile f = AudioFileIO.read(file);
            AudioHeader audioHeader= f.getAudioHeader();
            LocalTime time = LocalTime.parse("00:00:00", DateTimeFormatter.ISO_TIME);
            time = time.plusSeconds(Double.valueOf(audioHeader.getTrackLength()).longValue());

            modelMetaData.setTrackLengthIso(time);
            modelMetaData.setContentType(audioHeader.getEncodingType());
            modelMetaData.setChannels(audioHeader.getChannels());
            modelMetaData.setSampleRateHz(audioHeader.getSampleRate());
            modelMetaData.setBitRateKbps(audioHeader.getBitRate());

        } catch (CannotReadException | ReadOnlyFileException | TagException
                | IOException | InvalidAudioFrameException e) {
            log.error("getMetaDataFromAudioFile error : ", e);
        }
    }
}
