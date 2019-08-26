package com.trabeya.engineering.babelfish.controllers.dtos;

import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.trabeya.engineering.babelfish.model.SpeechToTextStreamingModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewSpeechToTextTranscriptionDto {

    @NotBlank(message = "Must specify a value")
    private String audioLanguageCode;

    @NotNull(message = "Must specify a value")
    private AudioEncoding audioEncoding;

    @NotNull(message = "Must specify a value")
    private int sampleRate;

    private SpeechToTextStreamingModel audioStreamingModel;

    private boolean isProfanityFilterEnabled;

    private boolean isEnhancedEnabled;

}
