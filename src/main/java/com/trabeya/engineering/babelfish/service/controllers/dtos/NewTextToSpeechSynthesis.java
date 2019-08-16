package com.trabeya.engineering.babelfish.service.controllers.dtos;

import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewTextToSpeechSynthesis {

    @NotBlank(message = "Must not be blank")
    private String inputText;

    @NotBlank(message = "Must not be blank")
    private String voiceLanguageCode;

    @NotNull(message = "Must specify a value")
    private SsmlVoiceGender gender;

    @NotNull(message = "Must specify a value")
    private AudioEncoding audioEncoding;

}
