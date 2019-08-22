package com.trabeya.engineering.babelfish.exceptions;

import com.trabeya.engineering.babelfish.controllers.dtos.NewTranslation;

public class TranslationLanguageNotSupportedException extends RuntimeException {

    public TranslationLanguageNotSupportedException(NewTranslation request)
    {
        super("Translation from : " + request.getInputLanguage()+ " to "
                +request.getOutputLanguage()+ " not supported");
    }
}