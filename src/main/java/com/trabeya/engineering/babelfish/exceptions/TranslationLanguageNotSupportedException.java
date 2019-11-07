package com.trabeya.engineering.babelfish.exceptions;

import com.trabeya.engineering.babelfish.controllers.dtos.NewTranslationRequest;

public class TranslationLanguageNotSupportedException extends RuntimeException {

    public TranslationLanguageNotSupportedException(NewTranslationRequest request)
    {
        super("Translation from : " + request.getInputLanguage()+ " to "
                +request.getOutputLanguage()+ " not supported");
    }
}