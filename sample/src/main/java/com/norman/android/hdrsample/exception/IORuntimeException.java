package com.norman.android.hdrsample.exception;

import java.io.IOException;

public class IORuntimeException extends RuntimeException{
    public IORuntimeException(IOException ioException) {
        super(ioException);
    }
}
