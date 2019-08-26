package com.trabeya.engineering.babelfish.controllers.advices;

import com.trabeya.engineering.babelfish.exceptions.TextToSpeechSynthesisFailedException;
import com.trabeya.engineering.babelfish.exceptions.TranslationFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class TextToSpeechFailedAdvice {

    @ResponseBody
    @ExceptionHandler(TextToSpeechSynthesisFailedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    String textToSpeechFailedHandler(TextToSpeechSynthesisFailedException ex) {
        return ex.getMessage();
    }
}