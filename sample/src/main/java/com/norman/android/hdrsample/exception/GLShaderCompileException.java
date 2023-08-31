package com.norman.android.hdrsample.exception;

public class GLShaderCompileException extends RuntimeException {
    public GLShaderCompileException(String message) {
        super(message);
    }

    public GLShaderCompileException(Throwable exception) {
        super(exception);
    }
}
