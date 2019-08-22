package com.trabeya.engineering.babelfish.client;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import com.trabeya.engineering.babelfish.exceptions.GoogleTextToSpeechSynthesisAPIException;
import com.trabeya.engineering.babelfish.model.TextToSpeechSynthesisDeviceProfile;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@Slf4j
public class GoogleTextToSpeechClient {

    private List<Voice> supportedVoiceList;

    private GoogleTextToSpeechClient() {
        // load supported voice list on client creation
        supportedVoiceList = listAllSupportedVoicesV1();
    }

    /**
     * Demonstrates using the Text to Speech client to list the client's supported voices.
     */
    public List<Voice> listAllSupportedVoicesV1() {

        // Instantiates a client
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Builds the text to speech list voices request
            ListVoicesRequest request = ListVoicesRequest.getDefaultInstance();

            // Performs the list voices request
            ListVoicesResponse response = textToSpeechClient.listVoices(request);
            List<Voice> voices = response.getVoicesList();

            for (Voice voice : voices) {
                // Display the voice's name. Example: tpc-vocoded
               log.debug(String.format("Name: %s%n", voice.getName()));

                // Display the supported language codes for this voice. Example: "en-us"
                List<ByteString> languageCodes = voice.getLanguageCodesList().asByteStringList();
                for (ByteString languageCode : languageCodes) {
                    log.debug(String.format("Supported Language: %s%n", languageCode.toStringUtf8()));
                }

                // Display the SSML Voice Gender
                log.debug(String.format("SSML Voice Gender: %s%n", voice.getSsmlGender()));

                // Display the natural sample rate hertz for this voice. Example: 24000
                log.debug(String.format("Natural Sample Rate Hertz: %s%n%n",
                        voice.getNaturalSampleRateHertz()));
            }
            return voices;
        }
        catch (Exception ex ) {
            log.error("listAllSupportedVoicesV1 service exception : ", ex);
            throw new GoogleTextToSpeechSynthesisAPIException(ex.getMessage());
        }
    }

    /**
     * Demonstrates using the Text to Speech client to synthesize text or ssml.
     *
     * @param text the raw text to be synthesized. (e.g., "Hello there!")
     */
    public byte[] synthesizeTextV1(String text, String languageCode, SsmlVoiceGender gender,
                                   AudioEncoding audioEncoding, TextToSpeechSynthesisDeviceProfile device,
                                    double speakingRate, double pitch) {
        // Instantiates a client
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // Build the voice request
            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode(languageCode) // eg: languageCode = "en_us"
                            .setSsmlGender(gender) // eg: ssmlVoiceGender = SsmlVoiceGender.FEMALE
                            .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig =
                    AudioConfig.newBuilder()
                            .setAudioEncoding(audioEncoding) // eg: MP3 audio.
                            .setSpeakingRate(speakingRate)
                            .setPitch(pitch)
                            .addEffectsProfileId(device.toString()
                                    .toLowerCase().replace("_","-")) // audio profile
                            .build();

            // Perform the text-to-speech request
            log.info("Text to audio synthesis initiated");
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();
            log.info("Text to audio synthesis complete");

            // Write the response to the output file.
            return audioContents.toByteArray();
        }
        catch (Exception ex ) {
            log.error("synthesizeTextV1 service exception : ", ex);
            throw new GoogleTextToSpeechSynthesisAPIException(ex.getMessage());
        }
    }

    /**
     * Demonstrates using the Text to Speech client to synthesize text or ssml.
     *
     * <p>Note: ssml must be well-formed according to: (https://www.w3.org/TR/speech-synthesis/
     * Example: <speak>Hello there.</speak>
     *
     * @param ssml the ssml document to be synthesized. (e.g., "<?xml...")
     */
    public byte[] synthesizeSsmlV1(String ssml, String languageCode, SsmlVoiceGender gender,
                                 AudioEncoding audioEncoding, TextToSpeechSynthesisDeviceProfile device,
                                   double speakingRate, double pitch) {
        // Instantiates a client
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Set the ssml input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder().setSsml(ssml).build();

            // Build the voice request
            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode(languageCode) // eg: languageCode = "en_us"
                            .setSsmlGender(gender) //eg: ssmlVoiceGender = SsmlVoiceGender.FEMALE
                            .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig =
                    AudioConfig.newBuilder()
                            .setAudioEncoding(audioEncoding) // eg: MP3 audio.
                            .setSpeakingRate(speakingRate)
                            .setPitch(pitch)
                            .addEffectsProfileId(device.toString()
                                    .toLowerCase().replace("_","-")) // eg: audio profile
                            .build();

            // Perform the text-to-speech request
            log.info("Text to audio synthesis initiated");
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();
            log.info("Text to audio synthesis complete");

            // Write the response to the output file.
            return audioContents.toByteArray();
        }
        catch (Exception ex ) {
            log.error("listAllSupportedVoicesV1 service exception : ", ex);
            throw new GoogleTextToSpeechSynthesisAPIException(ex.getMessage());
        }
    }

}
