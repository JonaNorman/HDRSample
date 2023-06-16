package com.norman.android.hdrsample.util;

public class ExceptionUtil {

    public static void throwRuntime(Throwable e) {
        RuntimeException runtimeException = new RuntimeException(e.getMessage());
        runtimeException.setStackTrace(e.getStackTrace());
        throw runtimeException;
    }
}
