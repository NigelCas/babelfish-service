package com.trabeya.engineering.babelfish.service.exceptions;

import com.trabeya.engineering.babelfish.service.model.TranslationModel;

public class TranslationFormatNotSupportedException extends RuntimeException {

    public TranslationFormatNotSupportedException(TranslationModel request)
    {
        super("Translation output format :" + request.getOutputFormat()+ "not supported");
    }
}