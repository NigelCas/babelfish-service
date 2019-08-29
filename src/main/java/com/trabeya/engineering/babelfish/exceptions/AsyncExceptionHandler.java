package com.trabeya.engineering.babelfish.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    /**
     * Handle the given uncaught exception thrown from an asynchronous method.
     *
     * @param ex     the exception thrown from the asynchronous method
     * @param method the asynchronous method
     * @param obj the parameters used to invoked the method
     */
    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... obj) {
        log.error("Exception message - " + ex.getMessage());
        log.error("Method name - " + method.getName());
        for (final Object param : obj) {
            log.error("Param - " + param);
        }
    }
}
