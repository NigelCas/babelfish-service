package com.trabeya.engineering.babelfish.service.exceptions;

import com.trabeya.engineering.babelfish.service.model.TranslationModel;

public class TranslationLanguageNotSupportedException extends RuntimeException {

    public TranslationLanguageNotSupportedException(TranslationModel request)
    {
        super("Translation from :" + request.getInputLanguage()+ "to "
                +request.getOutputLanguage()+ " not supported");
    }
}