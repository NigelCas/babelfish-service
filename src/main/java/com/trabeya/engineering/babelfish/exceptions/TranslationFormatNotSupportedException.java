package com.trabeya.engineering.babelfish.exceptions;

import com.trabeya.engineering.babelfish.controllers.dtos.NewTranslation;

public class TranslationFormatNotSupportedException extends RuntimeException {

    public TranslationFormatNotSupportedException(NewTranslation request)
    {
        super("Translation output format : " + request.getOutputFormat()+ " not supported");
    }
}