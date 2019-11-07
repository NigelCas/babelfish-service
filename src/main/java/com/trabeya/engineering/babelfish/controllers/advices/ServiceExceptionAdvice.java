package com.trabeya.engineering.babelfish.controllers.advices;

import com.trabeya.engineering.babelfish.exceptions.BabelFishServiceSystemException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ServiceExceptionAdvice {

    @ResponseBody
    @ExceptionHandler(BabelFishServiceSystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String serviceSystemExceptionHandler(BabelFishServiceSystemException ex) {
        return ex.getMessage();
    }
}
