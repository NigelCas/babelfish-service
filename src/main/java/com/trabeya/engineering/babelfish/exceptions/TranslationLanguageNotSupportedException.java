package com.trabeya.engineering.babelfish.exceptions;

import com.trabeya.engineering.babelfish.controllers.dtos.NewTranslationDto;

public class TranslationLanguageNotSupportedException extends RuntimeException {

    public TranslationLanguageNotSupportedException(NewTranslationDto request)
    {
        super("Translation from : " + request.getInputLanguage()+ " to "
                +request.getOutputLanguage()+ " not supported");
    }
}