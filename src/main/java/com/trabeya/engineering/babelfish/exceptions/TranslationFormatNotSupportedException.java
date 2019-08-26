package com.trabeya.engineering.babelfish.exceptions;

import com.trabeya.engineering.babelfish.controllers.dtos.NewTranslationDto;

public class TranslationFormatNotSupportedException extends RuntimeException {

    public TranslationFormatNotSupportedException(NewTranslationDto request)
    {
        super("Translation output format : " + request.getOutputFormat()+ " not supported");
    }
}