package com.norman.android.hdrsample.exception;

import java.io.IOException;

// 运行时的IO异常
public class IORuntimeException extends RuntimeException{
    public IORuntimeException(IOException ioException) {
        super(ioException);
    }
}
