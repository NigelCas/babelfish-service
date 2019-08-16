package com.trabeya.engineering.babelfish.service.exceptions;

import com.trabeya.engineering.babelfish.service.controllers.dtos.NewTextToSpeechSynthesis;

public class SynthesisFailedException extends RuntimeException {

    public SynthesisFailedException(NewTextToSpeechSynthesis request)
    {
        super("Text2speech synthesis : "+ "voice language code :" + request.getVoiceLanguageCode()+ " voice gender :"
        +request.getGender()+ " audio encoding : "+request.getAudioEncoding()+" not supported");
    }
}