package com.trabeya.engineering.babelfish.exceptions;

public class GoogleTranslationAPIException extends RuntimeException {

    public GoogleTranslationAPIException(String message) {
        super(message);
    }
}