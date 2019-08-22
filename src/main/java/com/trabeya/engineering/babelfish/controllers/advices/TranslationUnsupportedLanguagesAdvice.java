package com.trabeya.engineering.babelfish.controllers.advices;

import com.trabeya.engineering.babelfish.exceptions.TranslationLanguageNotSupportedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class TranslationUnsupportedLanguagesAdvice {

    @ResponseBody
    @ExceptionHandler(TranslationLanguageNotSupportedException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    String translationLanguageNotSupportedHandler(TranslationLanguageNotSupportedException ex) {
        return ex.getMessage();
    }
}