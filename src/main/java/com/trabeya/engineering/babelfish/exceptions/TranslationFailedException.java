package com.trabeya.engineering.babelfish.exceptions;

import com.trabeya.engineering.babelfish.controllers.dtos.NewTranslationRequest;

public class TranslationFailedException extends RuntimeException {

    public TranslationFailedException(NewTranslationRequest request)
    {
        super("Translation from :" + request.getInputLanguage()+ "to "
                +request.getOutputLanguage()+ " failed to execute!");
    }
}