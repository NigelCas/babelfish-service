package com.trabeya.engineering.babelfish.controllers.dtos;

import com.trabeya.engineering.babelfish.model.SpeechToTextStreamingModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewSpeechToTexV1TranscriptionRequest {

    @NotBlank(message = "Must specify a value")
    private String audioLanguageCode;

    private SpeechToTextStreamingModel audioStreamingModel;

    private boolean enableSeparateRecognitionPerChannel;

    private boolean isProfanityFilterEnabled;

    private boolean isEnhancedEnabled;

}
