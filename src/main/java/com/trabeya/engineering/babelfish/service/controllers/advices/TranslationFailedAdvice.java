package com.trabeya.engineering.babelfish.service.controllers.advices;

import com.trabeya.engineering.babelfish.service.exceptions.TranslationFailedException;
import com.trabeya.engineering.babelfish.service.exceptions.TranslationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class TranslationFailedAdvice {

    @ResponseBody
    @ExceptionHandler(TranslationFailedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    String translationFailedHandler(TranslationFailedException ex) {
        return ex.getMessage();
    }
}