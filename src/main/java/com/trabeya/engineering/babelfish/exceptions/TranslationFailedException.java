package com.trabeya.engineering.babelfish.exceptions;

import com.trabeya.engineering.babelfish.controllers.dtos.NewTranslationDto;

public class TranslationFailedException extends RuntimeException {

    public TranslationFailedException(NewTranslationDto request)
    {
        super("Translation from :" + request.getInputLanguage()+ "to "
                +request.getOutputLanguage()+ " failed to execute!");
    }
}