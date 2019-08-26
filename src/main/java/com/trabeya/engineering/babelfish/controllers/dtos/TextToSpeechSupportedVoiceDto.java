package com.trabeya.engineering.babelfish.controllers.dtos;

import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextToSpeechSupportedVoiceDto {

    private Object[] languageCodes;
    private String name;
    private SsmlVoiceGender ssmlGender;
    private int naturalSampleRateHertz;
}
