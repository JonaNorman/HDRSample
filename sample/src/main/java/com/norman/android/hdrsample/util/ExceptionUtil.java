package com.norman.android.hdrsample.util;

public class ExceptionUtil {

    public static RuntimeException throwRuntime(Throwable e) {
        RuntimeException runtimeException = new RuntimeException(e.getMessage());
        runtimeException.setStackTrace(e.getStackTrace());
        return runtimeException;
    }
}
