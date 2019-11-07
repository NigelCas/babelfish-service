package com.trabeya.engineering.babelfish.client.gcp;

import com.google.cloud.speech.v1p1beta1.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.protobuf.ByteString;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Data
@Slf4j
public class GoogleSpeechToTextV1P1Beta1Client {

    /**
     * Transcribe a local audio file with multi-language recognition
     *
     * @param fileName the path to the audio file
     */
    public static void transcribeMultiLanguage(String fileName) throws Exception {
        Path path = Paths.get(fileName);
        // Get the contents of the local audio file
        byte[] content = Files.readAllBytes(path);

        try (SpeechClient speechClient = SpeechClient.create()) {

            RecognitionAudio recognitionAudio =
                    RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(content)).build();
            ArrayList<String> languageList = new ArrayList<>();
            languageList.add("es-ES");
            languageList.add("en-US");

            // Configure request to enable multiple languages
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setSampleRateHertz(16000)
                            .setLanguageCode("ja-JP")
                            .addAllAlternativeLanguageCodes(languageList)
                            .build();
            // Perform the transcription request
            RecognizeResponse recognizeResponse = speechClient.recognize(config, recognitionAudio);

            // Print out the results
            for (SpeechRecognitionResult result : recognizeResponse.getResultsList()) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternatives(0);
                System.out.format("Transcript : %s\n\n", alternative.getTranscript());
            }
        }
    }

    /**
     * Performs synchronous speech recognition with speech adaptation.
     *
     * @param sampleRateHertz Sample rate in Hertz of the audio data sent in all `RecognitionAudio`
     *     messages. Valid values are: 8000-48000.
     * @param languageCode The language of the supplied audio.
     * @param phrase Phrase "hints" help Speech-to-Text API recognize the specified phrases from your
     *     audio data.
     * @param boost Positive value will increase the probability that a specific phrase will be
     *     recognized over other similar sounding phrases.
     * @param uriPath Path to the audio file stored on GCS.
     */
    public static void sampleRecognize(
            int sampleRateHertz, String languageCode, String phrase, float boost, String uriPath) {
        try (SpeechClient speechClient = SpeechClient.create()) {
            // sampleRateHertz = 44100;
            // languageCode = "en-US";
            // phrase = "Brooklyn Bridge";
            // boost = 20.0F;
            // uriPath = "gs://cloud-samples-data/speech/brooklyn_bridge.mp3";
            RecognitionConfig.AudioEncoding encoding = RecognitionConfig.AudioEncoding.MP3;
            List<String> phrases = Arrays.asList(phrase);
            SpeechContext speechContextsElement =
                    SpeechContext.newBuilder().addAllPhrases(phrases).setBoost(boost).build();
            List<SpeechContext> speechContexts = Arrays.asList(speechContextsElement);
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(encoding)
                            .setSampleRateHertz(sampleRateHertz)
                            .setLanguageCode(languageCode)
                            .addAllSpeechContexts(speechContexts)
                            .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(uriPath).build();
            RecognizeRequest request =
                    RecognizeRequest.newBuilder().setConfig(config).setAudio(audio).build();
            RecognizeResponse response = speechClient.recognize(request);
            for (SpeechRecognitionResult result : response.getResultsList()) {
                // First alternative is the most probable result
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                System.out.printf("Transcript: %s\n", alternative.getTranscript());
            }
        } catch (Exception exception) {
            System.err.println("Failed to create the client due to: " + exception);
        }
    }

    /**
     * Transcribe the given audio file using speaker diarization.
     *
     * @param fileName the path to an audio file.
     */
    public static void transcribeDiarization(String fileName) throws Exception {
        Path path = Paths.get(fileName);
        byte[] content = Files.readAllBytes(path);

        try (SpeechClient speechClient = SpeechClient.create()) {
            // Get the contents of the local audio file
            RecognitionAudio recognitionAudio =
                    RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(content)).build();

            // Configure request to enable Speaker diarization
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setLanguageCode("en-US")
                            .setSampleRateHertz(8000)
                            .setEnableSpeakerDiarization(true)
                            .setDiarizationSpeakerCount(2)
                            .build();

            // Perform the transcription request
            RecognizeResponse recognizeResponse = speechClient.recognize(config, recognitionAudio);

            // Print out the results
            for (SpeechRecognitionResult result : recognizeResponse.getResultsList()) {
                // There can be several alternative transcripts for a given chunk of speech. Just
                // use the first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternatives(0);
                System.out.format("Transcript : %s\n", alternative.getTranscript());
                // The words array contains the entire transcript up until that point.
                // Referencing the last spoken word to get the associated Speaker tag
                System.out.format(
                        "Speaker Tag %s: %s\n",
                        alternative.getWords((alternative.getWordsCount() - 1)).getSpeakerTag(),
                        alternative.getTranscript());
            }
        }
    }

    /**
     * Transcribe the given audio file and include recognition metadata in the request.
     *
     * @param fileName the path to an audio file.
     */
    public static void transcribeFileWithMetadata(String fileName) throws Exception {
        Path path = Paths.get(fileName);
        byte[] content = Files.readAllBytes(path);

        try (SpeechClient speechClient = SpeechClient.create()) {
            // Get the contents of the local audio file
            RecognitionAudio recognitionAudio =
                    RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(content)).build();

            // Construct a recognition metadata object.
            // Most metadata fields are specified as enums that can be found
            // in speech.enums.RecognitionMetadata
            RecognitionMetadata metadata =
                    RecognitionMetadata.newBuilder()
                            .setInteractionType(RecognitionMetadata.InteractionType.DISCUSSION)
                            .setMicrophoneDistance(RecognitionMetadata.MicrophoneDistance.NEARFIELD)
                            .setRecordingDeviceType(RecognitionMetadata.RecordingDeviceType.SMARTPHONE)
                            .setRecordingDeviceName("Pixel 2 XL") // Some metadata fields are free form strings
                            // And some are integers, for instance the 6 digit NAICS code
                            // https://www.naics.com/search/
                            .setIndustryNaicsCodeOfAudio(519190)
                            .build();

            // Configure request to enable enhanced models
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setLanguageCode("en-US")
                            .setSampleRateHertz(8000)
                            .setMetadata(metadata) // Add the metadata to the config
                            .build();

            // Perform the transcription request
            RecognizeResponse recognizeResponse = speechClient.recognize(config, recognitionAudio);

            // Print out the results
            for (SpeechRecognitionResult result : recognizeResponse.getResultsList()) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternatives(0);
                System.out.format("Transcript: %s\n\n", alternative.getTranscript());
            }
        }
    }


}
