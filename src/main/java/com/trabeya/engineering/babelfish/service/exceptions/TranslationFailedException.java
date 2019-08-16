package com.trabeya.engineering.babelfish.service.exceptions;

import com.trabeya.engineering.babelfish.service.controllers.dtos.NewTranslation;

public class TranslationFailedException extends RuntimeException {

    public TranslationFailedException(NewTranslation request)
    {
        super("Translation from :" + request.getInputLanguage()+ "to "
                +request.getOutputLanguage()+ " failed to execute!");
    }
}