package com.trabeya.engineering.babelfish.exceptions;

public class TranslationNotFoundException extends RuntimeException {

    public TranslationNotFoundException(Long id) {
        super("Could not find translation " + id);
    }
}