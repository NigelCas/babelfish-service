package com.trabeya.engineering.babelfish.controllers.dtos;

import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.trabeya.engineering.babelfish.model.TextToSpeechSynthesisDeviceProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewTextToSpeechSynthesisDto {

    @NotBlank(message = "Must not be blank")
    private String inputText;

    @NotBlank(message = "Must specify a valid value, " +
            "supported voice languages; https://cloud.google.com/text-to-speech/docs/voices")
    private String voiceLanguageCode;

    @NotNull(message = "Must specify a valid value, " +
            "defined in; https://www.w3.org/TR/speech-synthesis11/#edef_voice")
    private SsmlVoiceGender voiceGender;

    @NotNull(message = "Must specify a value, " +
            "supported audio encodes; https://cloud.google.com/text-to-speech/docs/" +
            "reference/rest/v1/text/synthesize#AudioEncoding")
    private AudioEncoding audioEncoding;

    @NotNull(message = "Must specify a value, " +
            "supported audio device profiles; https://cloud.google.com/text-to-speech/docs/audio-profiles")
    private TextToSpeechSynthesisDeviceProfile audioDeviceProfile;

    @DecimalMax(value = "4.0", message = "Value more than required")
    @DecimalMin(value = "0.0", message = "Value less than required")
    private BigDecimal audioSpeakingRate;

    @DecimalMax(value = "20.0", message = "Value more than required")
    @DecimalMin(value = "-20.0", message = "Value less than required")
    private BigDecimal pitch;

}
