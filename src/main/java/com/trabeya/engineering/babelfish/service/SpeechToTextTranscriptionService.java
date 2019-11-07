package com.trabeya.engineering.babelfish.service;

import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.trabeya.engineering.babelfish.client.gcp.GoogleSpeechToTextV1Client;
import com.trabeya.engineering.babelfish.model.SpeechToTextStreamingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SpeechToTextTranscriptionService {

    @Autowired
    private GoogleSpeechToTextV1Client googleSpeechToTextV1Client;

    public void startRealTimeSpeechToTextTranscription
        (AudioEncoding targetAudioEncoding,String languageCode,int sampleRate,SpeechToTextStreamingModel model,
         boolean profanityFilter, boolean useEnhanced) {
        log.info("Service - startSpeechToTextTranscription() - params - {},{},{},{}",
                targetAudioEncoding,languageCode,sampleRate,model);
//        googleSpeechToTextV1Client.streamingRealtimeRecognizeLocalV1Init(
//                targetAudioEncoding,languageCode, sampleRate, model, profanityFilter, useEnhanced);
        log.info("Service - startSpeechToTextTranscription() completed ");
    }

    public void transmitRealTimeSpeechToTextTranscription(byte[] audioData) {
//        googleSpeechToTextV1Client.streamingRealtimeRecognizeLocalV1Transmit(audioData);
        log.info("Service - transmitRealTimeSpeechToTextTranscription() <>");
    }

    public void completeRealTimeSpeechToTextTranscription() {
//        googleSpeechToTextV1Client.streamingRealtimeRecognizeLocalV1Complete();
        log.info("Service - completeRealTimeSpeechToTextTranscription() completed");
    }
}
