package com.trabeya.engineering.babelfish.controllers.websocket.dtos;

import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.trabeya.engineering.babelfish.model.SpeechToTextStreamingModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewRealTimeSpeechToTextV1TranscriptionDto {

    @NotBlank(message = "Must specify a value")
    private String audioLanguageCode;

    @NotNull
    private SpeechToTextStreamingModel audioStreamingModel;

    @NotNull
    private AudioEncoding targetAudioEncoding;

    @NotNull
    private int sampleRate;

    private boolean isProfanityFilterEnabled;

    private boolean isEnhancedEnabled;
}
