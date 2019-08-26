package com.trabeya.engineering.babelfish.client;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.api.gax.rpc.BidiStreamingCallable;
import com.google.cloud.speech.v1.*;
import com.google.cloud.speech.v1.RecognitionConfig.*;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.ByteString;
import com.trabeya.engineering.babelfish.exceptions.GoogleTextToSpeechSynthesisAPIException;
import com.trabeya.engineering.babelfish.model.SpeechToTextStreamingModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@Slf4j
public class GoogleSpeechToTextClient {

    private ResponseApiStreamingObserver<StreamingRecognizeResponse> responseObserver;

    private GoogleSpeechToTextClient() {
        responseObserver = new ResponseApiStreamingObserver<>();
    }

    /**
     * Performs speech recognition on raw audio and prints the transcription.
     *
     * @param uploadedFileData the raw data from audio file to transcribe.
     */
    public void syncRecognizeFileV1(byte[] uploadedFileData,
                                           AudioEncoding audioEncoding,
                                           String languageCodes,
                                           int sampleRateHertz,
                                           SpeechToTextStreamingModel model,
                                           List<String> commonSpeechHints,
                                           boolean profanityFilterEnabled,
                                           boolean modelEnhancementEnabled
                                           )  {
        try (SpeechClient speech = SpeechClient.create()) {

            // Convert raw bytes to Base64 encoded byte string
            ByteString audioBytes = ByteString.copyFrom(uploadedFileData);

            // Add helpful speech hints to increase accuracy
            SpeechContext sc = SpeechContext.newBuilder()
                    .addAllPhrases(commonSpeechHints)
                    .build();

            // Configure request with local raw audio
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(audioEncoding)
                            .setLanguageCode(languageCodes)
                            .setSampleRateHertz(sampleRateHertz)
                            .setModel(model.toString().toLowerCase())
                            .setSpeechContexts(0,sc)
                            .setProfanityFilter(profanityFilterEnabled)
                            .setUseEnhanced(modelEnhancementEnabled)
                            .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();

            // Use blocking call to get audio transcript
            RecognizeResponse response = speech.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                log.debug(String.format("Transcription: %s%n", alternative.getTranscript()));
            }
        }
        catch (Exception ex) {
            log.error("syncRecognizeFileV1 service exception : ", ex);
            throw new GoogleTextToSpeechSynthesisAPIException(ex.getMessage());
        }
    }


    /**
     * Performs speech recognition on remote file and prints the transcription.
     *
     * @param gcsUri the path to the remote audio file to transcribe.
     */
    public void syncRecognizeGcsV1(String gcsUri,
                                   AudioEncoding audioEncoding,
                                   String languageCodes,
                                   int sampleRateHertz,
                                   SpeechToTextStreamingModel model,
                                   List<String> commonSpeechHints,
                                   boolean profanityFilterEnabled,
                                   boolean modelEnhancementEnabled) {

        // Instantiates a client with GOOGLE_APPLICATION_CREDENTIALS
        try (SpeechClient speech = SpeechClient.create()) {

            // Builds the request for remote file
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(audioEncoding)
                            .setLanguageCode(languageCodes)
                            .setSampleRateHertz(sampleRateHertz)
                            .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(gcsUri).build();

            // Use blocking call for getting audio transcript
            RecognizeResponse response = speech.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                System.out.printf("Transcription: %s%n", alternative.getTranscript());
            }
        }
        catch (Exception ex) {
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
                            .setSpeechContexts(0,sc)
                            .setSampleRateHertz(sampleRateHertz)
                            .setModel(model.toString().toLowerCase())
                            .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(gcsUri).build();

            // Use non-blocking call for getting file transcription
            response = speech.longRunningRecognizeAsync(config, audio);

        }
        catch (Exception ex) {
            log.error("asyncRecognizeGcsV1 service exception : ", ex);
            throw new GoogleTextToSpeechSynthesisAPIException(ex.getMessage());
        }
        return response;
    }

    /**
     *
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
            }
            catch (Exception ex) {
                log.error("asyncRecognizeGcsV1Response service exception : ", ex);
                throw new GoogleTextToSpeechSynthesisAPIException(ex.getMessage());
            }
            return finalTranscript.toString();
    }



    /**
     * Starts a streaming speech recognition on raw PCM local data.
     */
    public ApiStreamObserver<StreamingRecognizeRequest> streamingRecognizeLocalV1Init(
            AudioEncoding audioEncoding,
            String languageCodes,
            int sampleRateHertz,
            SpeechToTextStreamingModel model
    ) {

        ApiStreamObserver<StreamingRecognizeRequest> requestObserver;
        // Instantiates a client with GOOGLE_APPLICATION_CREDENTIALS
        try (SpeechClient speech = SpeechClient.create()) {

            // Configure request with local raw PCM audio
            RecognitionConfig recConfig =
                    RecognitionConfig.newBuilder()
                            .setEncoding(audioEncoding)
                            .setLanguageCode(languageCodes)
                            .setSampleRateHertz(sampleRateHertz)
                            .setModel(model.toString().toLowerCase())
                            .build();
            StreamingRecognitionConfig config =
                    StreamingRecognitionConfig.newBuilder().setConfig(recConfig).build();

            BidiStreamingCallable<StreamingRecognizeRequest, StreamingRecognizeResponse> callable =
                    speech.streamingRecognizeCallable();

            requestObserver = callable.bidiStreamingCall(responseObserver);

            // Throw Exception in case of stream failures
            requestObserver.onError(new GoogleTextToSpeechSynthesisAPIException("Stream failed to process"));

            // The first request must **only** contain the audio configuration:
            requestObserver.onNext(
                    StreamingRecognizeRequest.newBuilder().setStreamingConfig(config).build());

        }
        catch (Exception ex) {
            log.error("asyncRecognizeGcsV1Response service exception : ", ex);
            throw new GoogleTextToSpeechSynthesisAPIException(ex.getMessage());
        }
        return requestObserver;
    }

    /**
     * continues streaming speech recognition on raw local data(callable multiple times).
     * @param data byte array of audio file to transcribe.
     * @param requestObserver request passed from streamingRecognizeLocal_init()
     */
    public void streamingRecognizeLocalV1EnQueue(
            ApiStreamObserver<StreamingRecognizeRequest> requestObserver,
            byte[] data){

        // Subsequent requests must **only** contain the audio data.
        requestObserver.onNext(
                StreamingRecognizeRequest.newBuilder()
                        .setAudioContent(ByteString.copyFrom(data))
                        .build());
    }


    public String streamingRecognizeLocalTranscript(
            ApiStreamObserver<StreamingRecognizeRequest> requestObserver) {

        StringBuilder finalTranscript = new StringBuilder();
        try {
            // Mark transmission as completed after sending the data.
            requestObserver.onCompleted();
            List<StreamingRecognizeResponse> responses = responseObserver.future().get();

            for (StreamingRecognizeResponse response : responses) {
                // For streaming recognize, the results list has one is_final result (if available) followed
                // by a number of in-progress results (if iterim_results is true) for subsequent utterances.
                // Just print the first result here.
                StreamingRecognitionResult result = response.getResultsList().get(0);
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                finalTranscript.append(alternative.getTranscript());
            }
        }
        catch (Exception ex) {
            log.error("streamingRecognizeLocal_response service exception : ", ex);
            throw new GoogleTextToSpeechSynthesisAPIException(ex.getMessage());
        }
        return finalTranscript.toString();
    }

    static class ResponseApiStreamingObserver<T> implements ApiStreamObserver<T> {
        private final SettableFuture<List<T>> future = SettableFuture.create();
        private final List<T> messages = new java.util.ArrayList<>();

        @Override
        public void onNext(T message) {
            messages.add(message);
        }

        @Override
        public void onError(Throwable t) {
            future.setException(t);
        }

        @Override
        public void onCompleted() {
            future.set(messages);
        }

        // Returns the SettableFuture object to get received messages / exceptions.
        SettableFuture<List<T>> future() {
            return future;
        }
    }

}
