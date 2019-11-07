package com.trabeya.engineering.babelfish.client.gcp;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.rpc.*;
import com.google.cloud.speech.v1.*;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.ByteString;
import com.trabeya.engineering.babelfish.controllers.websocket.dtos.NewRealTimeSpeechToTextV1TranscriptionDto;
import com.trabeya.engineering.babelfish.exceptions.GoogleTextToSpeechSynthesisAPIException;
import com.trabeya.engineering.babelfish.model.SpeechToTextStreamingModel;
import com.trabeya.engineering.babelfish.queue.EnqueueMessage;
import com.trabeya.engineering.babelfish.queue.dto.TranscriptionDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Data
@Slf4j
public class GoogleSpeechToTextV1Client {

    @Autowired
    private JmsTemplate jmsTemplate;

    private NewRealTimeSpeechToTextV1TranscriptionDto newRealTimeSpeechToTextV1TranscriptionDto = null;


    /**
     * Performs speech recognition on raw audio and prints the transcription.
     *
     * @param uploadedFileData the raw data from audio file to transcribe.
     */
    public String syncRecognizeFileV1(byte[] uploadedFileData, RecognitionConfig config) {
        try (SpeechClient speech = SpeechClient.create()) {

            StringBuilder finalTranscript = new StringBuilder();
            // Convert raw bytes to Base64 encoded byte string
            ByteString audioBytes = ByteString.copyFrom(uploadedFileData);

            // Configure request with local raw audio
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();

            // Use blocking call to get audio transcript
            RecognizeResponse response = speech.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternatives(0);
                finalTranscript.append(alternative.getTranscript());
                log.debug(String.format("Transcription: %s%n", alternative.getTranscript()));
                log.debug(String.format("Channel Tag : %s%n", result.getChannelTag()));
            }
            return finalTranscript.toString();
        } catch (Exception ex) {
            log.error("syncRecognizeFileV1 service exception : ", ex);
            throw new GoogleTextToSpeechSynthesisAPIException(ex.getMessage());
        }
    }


    /**
     * Performs speech recognition on remote file and prints the transcription.
     *
     * @param gcsUri the path to the remote audio file to transcribe.
     */
    public String syncRecognizeGcsV1(String gcsUri, RecognitionConfig config) {

        // Instantiates a client with GOOGLE_APPLICATION_CREDENTIALS
        try (SpeechClient speech = SpeechClient.create()) {

            StringBuilder finalTranscript = new StringBuilder();
            // Builds the request for remote file
            RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(gcsUri).build();

            // Use blocking call for getting audio transcript
            RecognizeResponse response = speech.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternatives(0);
                finalTranscript.append(alternative.getTranscript());
                log.debug(String.format("Transcription: %s%n", alternative.getTranscript()));
                log.debug(String.format("Channel Tag : %s%n", result.getChannelTag()));
            }
            return finalTranscript.toString();
        } catch (Exception ex) {
            log.error("syncRecognizeGcsV1 service exception : ", ex);
            throw new GoogleTextToSpeechSynthesisAPIException(ex.getMessage());
        }
    }


    /**
     * Performs non-blocking speech recognition on remote audio file and prints the transcription.
     *
     * @param gcsUri the path to the remote audio file to transcribe.
     */
    public OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata>
    asyncRecognizeGcsV1Request(String gcsUri,
                               AudioEncoding audioEncoding,
                               String languageCodes,
                               int sampleRateHertz,
                               SpeechToTextStreamingModel model,
                               List<String> commonSpeechHints) {
        OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response;

        // Instantiates a client with GOOGLE_APPLICATION_CREDENTIALS
        try (SpeechClient speech = SpeechClient.create()) {

            // Add helpful speech hints to increase accuracy
            SpeechContext sc = SpeechContext.newBuilder()
                    .addAllPhrases(commonSpeechHints)
                    .build();

            // Configure remote file request for FLAC
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(audioEncoding)
                            .setLanguageCode(languageCodes)
                            .setSpeechContexts(0, sc)
                            .setSampleRateHertz(sampleRateHertz)
                            .setModel(model.toString().toLowerCase())
                            .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(gcsUri).build();

            // Use non-blocking call for getting file transcription
            response = speech.longRunningRecognizeAsync(config, audio);

        } catch (Exception ex) {
            log.error("asyncRecognizeGcsV1 service exception : ", ex);
            throw new GoogleTextToSpeechSynthesisAPIException(ex.getMessage());
        }
        return response;
    }

    /**
     * @param response
     * @param responseSleep
     * @return final transcription
     */
    public String asyncRecognizeGcsV1Response(
            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response,
            int responseSleep) {
        StringBuilder finalTranscript = new StringBuilder();
        try {
            while (!response.isDone()) {
                log.info("Waiting for response...");
                Thread.sleep(responseSleep);
            }

            List<SpeechRecognitionResult> results = response.get().getResultsList();
            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                finalTranscript.append(alternative.getTranscript());
            }
        } catch (Exception ex) {
            log.error("asyncRecognizeGcsV1Response service exception : ", ex);
            throw new GoogleTextToSpeechSynthesisAPIException(ex.getMessage());
        }
        return finalTranscript.toString();
    }

    /**
     * Performs speech recognition on audio data
     *
     * @param data the data of audio file to transcribe.
     */
    public void syncRecognizeFile(byte[] data,
                                  AudioEncoding audioEncoding,
                                  int sampleRateHertz,
                                  String languageCode,
                                  SpeechToTextStreamingModel model,
                                  boolean setProfanityFilter,
                                  boolean useEnhanced) {
        try (SpeechClient speech = SpeechClient.create()) {
            ByteString audioBytes = ByteString.copyFrom(data);

            // Configure request with local raw PCM audio
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(audioEncoding)
                            .setLanguageCode(languageCode)
                            .setSampleRateHertz(sampleRateHertz)
                            .setModel(model.toString().toLowerCase())
                            .setProfanityFilter(setProfanityFilter)
                            .setUseEnhanced(useEnhanced)
                            .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();

            // Use blocking call to get audio transcript
            RecognizeResponse response = speech.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();
            int iCount = 1;
            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                jmsTemplate.convertAndSend("/service/transcriptions/realtime/response",
                        alternative.getTranscript());
            }
        }
        catch (Exception ex) {
            log.error("syncRecognizeFile() failed to complete :", ex);
        }
    }

    public void syncRecognizeFile(byte[] data) {
        try (SpeechClient speech = SpeechClient.create()) {
            ByteString audioBytes = ByteString.copyFrom(data);

            // Configure request with local raw PCM audio
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(newRealTimeSpeechToTextV1TranscriptionDto.getTargetAudioEncoding())
                            .setLanguageCode(newRealTimeSpeechToTextV1TranscriptionDto.getAudioLanguageCode())
                            .setSampleRateHertz(newRealTimeSpeechToTextV1TranscriptionDto.getSampleRate())
                            .setModel(newRealTimeSpeechToTextV1TranscriptionDto.getAudioStreamingModel()
                                    .toString().toLowerCase())
                            .setProfanityFilter(newRealTimeSpeechToTextV1TranscriptionDto.isProfanityFilterEnabled())
                            .setUseEnhanced(newRealTimeSpeechToTextV1TranscriptionDto.isEnhancedEnabled())
                            .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();

            // Use blocking call to get audio transcript
            RecognizeResponse response = speech.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();
            int iCount = 1;
            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                String responseResult = alternative.getTranscript();
                log.info("Transcription: {}", responseResult);
                jmsTemplate.convertAndSend("/service/transcriptions/realtime/response",
                        responseResult);
            }
        }
        catch (Exception ex) {
            log.error("syncRecognizeFile() failed to complete :", ex);
        }

    }

}
