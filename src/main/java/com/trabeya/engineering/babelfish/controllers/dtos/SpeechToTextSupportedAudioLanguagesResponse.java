package com.trabeya.engineering.babelfish.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpeechToTextSupportedAudioLanguagesResponse {

    private String language;
    private String languageCode;
    private String languageAngloName;

}
