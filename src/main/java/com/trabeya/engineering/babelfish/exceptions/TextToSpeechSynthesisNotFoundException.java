package com.trabeya.engineering.babelfish.exceptions;

public class TextToSpeechSynthesisNotFoundException extends RuntimeException {

    public TextToSpeechSynthesisNotFoundException(Long id) {
        super("Could not find text-to-speech synthesis entry : " + id);
    }
}