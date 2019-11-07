package com.trabeya.engineering.babelfish.exceptions;

import com.trabeya.engineering.babelfish.controllers.dtos.NewTranslationRequest;

public class TranslationFormatNotSupportedException extends RuntimeException {

    public TranslationFormatNotSupportedException(NewTranslationRequest request)
    {
        super("Translation output format : " + request.getOutputFormat()+ " not supported");
    }
}