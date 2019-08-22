package com.trabeya.engineering.babelfish.exceptions;

import com.trabeya.engineering.babelfish.controllers.dtos.NewTranslation;

public class TranslationFailedException extends RuntimeException {

    public TranslationFailedException(NewTranslation request)
    {
        super("Translation from :" + request.getInputLanguage()+ "to "
                +request.getOutputLanguage()+ " failed to execute!");
    }
}