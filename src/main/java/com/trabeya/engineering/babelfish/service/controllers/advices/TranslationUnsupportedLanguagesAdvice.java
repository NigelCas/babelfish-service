package com.trabeya.engineering.babelfish.service.controllers.advices;

import com.trabeya.engineering.babelfish.service.exceptions.TranslationLanguageNotSupportedException;
import com.trabeya.engineering.babelfish.service.exceptions.TranslationNotFoundException;
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
    String translationNotFoundHandler(TranslationLanguageNotSupportedException ex) {
        return ex.getMessage();
    }
}