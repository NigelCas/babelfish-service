package com.trabeya.engineering.babelfish.exceptions;

import com.trabeya.engineering.babelfish.controllers.dtos.NewTextToSpeechSynthesisRequest;

public class SpeechToTextSynthesisFailedException extends RuntimeException {

    public SpeechToTextSynthesisFailedException(NewTextToSpeechSynthesisRequest request)
    {
        super("Text2speech synthesis : "+ "voice language code :" + request.getVoiceLanguageCode()+ " voice gender :"
        +request.getVoiceGender()+ " audio encoding : "+request.getAudioEncoding()+" not supported");
    }
}