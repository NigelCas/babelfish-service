package com.trabeya.engineering.babelfish.exceptions;

import com.trabeya.engineering.babelfish.controllers.dtos.NewSsmlToSpeechSynthesisRequest;

public class SsmlToSpeechSynthesisFailedException extends RuntimeException {

    public SsmlToSpeechSynthesisFailedException(NewSsmlToSpeechSynthesisRequest request)
    {
        super("Text2speech synthesis : "+ "voice language code :" + request.getVoiceLanguageCode()+ " voice gender :"
        +request.getVoiceGender()+ " audio encoding : "+request.getAudioEncoding()+" not supported");
    }
}