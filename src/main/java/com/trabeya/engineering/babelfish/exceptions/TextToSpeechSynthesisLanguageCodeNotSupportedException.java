package com.trabeya.engineering.babelfish.exceptions;

import com.trabeya.engineering.babelfish.controllers.dtos.NewTextToSpeechSynthesisDto;

public class TextToSpeechSynthesisLanguageCodeNotSupportedException extends RuntimeException {

    public TextToSpeechSynthesisLanguageCodeNotSupportedException(NewTextToSpeechSynthesisDto request)
    {
        super("Translation voice language code :" + request.getVoiceLanguageCode()+ " not supported");
    }
}