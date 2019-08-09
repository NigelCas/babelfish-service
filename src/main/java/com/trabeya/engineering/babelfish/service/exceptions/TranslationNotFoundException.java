package com.trabeya.engineering.babelfish.service.exceptions;

public class TranslationNotFoundException extends RuntimeException {

    public TranslationNotFoundException(Long id) {
        super("Could not find translation " + id);
    }
}