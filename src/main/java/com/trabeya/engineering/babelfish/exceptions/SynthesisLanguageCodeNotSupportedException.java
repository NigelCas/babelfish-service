package com.trabeya.engineering.babelfish.exceptions;

import com.trabeya.engineering.babelfish.controllers.dtos.NewTextToSpeechSynthesis;

public class SynthesisLanguageCodeNotSupportedException extends RuntimeException {

    public SynthesisLanguageCodeNotSupportedException(NewTextToSpeechSynthesis request)
    {
        super("Translation voice language code :" + request.getVoiceLanguageCode()+ " not supported");
    }
}