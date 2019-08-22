package com.trabeya.engineering.babelfish.exceptions;

public class GoogleCloudStorageFailedException extends RuntimeException {

    public GoogleCloudStorageFailedException(String message) {
        super(message);
    }
}