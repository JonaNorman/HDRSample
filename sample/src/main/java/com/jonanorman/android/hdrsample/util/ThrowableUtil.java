package com.jonanorman.android.hdrsample.util;

public class ThrowableUtil {

    public static void throwException(Exception e) {
        RuntimeException runtimeException = new RuntimeException(e.getMessage());
        runtimeException.setStackTrace(e.getStackTrace());
        throw runtimeException;
    }
}
