package com.norman.android.hdrsample.exception;

public class RuntimeException extends java.lang.RuntimeException {

    public RuntimeException(String message) {
        super(message);
    }

    public RuntimeException(Throwable exception) {
        super(exception);
        setStackTrace(exception.getStackTrace());
    }
}
