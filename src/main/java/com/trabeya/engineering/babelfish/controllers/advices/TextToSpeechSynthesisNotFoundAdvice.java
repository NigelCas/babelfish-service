package com.trabeya.engineering.babelfish.controllers.advices;

import com.trabeya.engineering.babelfish.exceptions.TextToSpeechSynthesisNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class TextToSpeechSynthesisNotFoundAdvice {

    @ResponseBody
    @ExceptionHandler(TextToSpeechSynthesisNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String translationNotFoundHandler(TextToSpeechSynthesisNotFoundException ex) {
        return ex.getMessage();
    }
}