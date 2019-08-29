package com.trabeya.engineering.babelfish.controllers.dtos;

import com.trabeya.engineering.babelfish.model.SpeechToTextStreamingModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewSpeechToTextRemoteTranscriptionDto {

    @NotBlank(message = "Must specify a value")
    private String remoteURI;

    @NotBlank(message = "Must specify a value")
    private String audioLanguageCode;

    private String[] speechContextsHints;

    private SpeechToTextStreamingModel audioStreamingModel;

    private boolean enableSeparateRecognitionPerChannel;

    private boolean isProfanityFilterEnabled;

    private boolean isEnhancedEnabled;

}
