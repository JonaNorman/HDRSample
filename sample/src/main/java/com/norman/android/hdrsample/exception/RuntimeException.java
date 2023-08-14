package com.norman.android.hdrsample.exception;

//复写RuntimeException
public class RuntimeException extends java.lang.RuntimeException {

    public RuntimeException(String message) {
        super(message);
    }

    //传递RuntimeException时可以保证正确的堆栈日志
    public RuntimeException(Throwable exception) {
        super(exception);
        setStackTrace(exception.getStackTrace());
    }
}
