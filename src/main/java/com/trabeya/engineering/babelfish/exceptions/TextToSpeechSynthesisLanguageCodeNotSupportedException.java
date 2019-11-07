package com.trabeya.engineering.babelfish.exceptions;

import com.trabeya.engineering.babelfish.controllers.dtos.NewTextToSpeechSynthesisRequest;

public class TextToSpeechSynthesisLanguageCodeNotSupportedException extends RuntimeException {

    public TextToSpeechSynthesisLanguageCodeNotSupportedException(NewTextToSpeechSynthesisRequest request)
    {
        super("Translation voice language code :" + request.getVoiceLanguageCode()+ " not supported");
    }
}