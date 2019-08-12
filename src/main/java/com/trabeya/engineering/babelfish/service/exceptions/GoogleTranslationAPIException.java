package com.trabeya.engineering.babelfish.service.exceptions;

public class GoogleTranslationAPIException extends RuntimeException {

    public GoogleTranslationAPIException(String message) {
        super(message);
    }
}