package com.norman.android.hdrsample.exception;

import java.io.IOException;

// 运行时的IO异常，为了让IO异常不用throw
public class IORuntimeException extends RuntimeException{
    public IORuntimeException(IOException ioException) {
        super(ioException);
    }
}
