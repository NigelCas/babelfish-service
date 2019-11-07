package com.trabeya.engineering.babelfish.exceptions;

public class BabelFishServiceSystemException extends RuntimeException {

    public BabelFishServiceSystemException(String message) {
        super("Unable to complete request : " + message);
    }
}