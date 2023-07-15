package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;

public class GLEnvException extends RuntimeException {
    private final int errorCode;

    public GLEnvException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    static void checkError() {
        while (true) {
            int error = EGL14.eglGetError();
            if (error == EGL14.EGL_SUCCESS) {
                break;
            }
            String message = getMessage(error);
            throw new GLEnvException(error, message);
        }
    }


    static void clearError() {
        while (true) {
            int error = EGL14.eglGetError();
            if (error == EGL14.EGL_SUCCESS) {
                break;
            }
        }
    }


    private static String getMessage(int error) {
        String message;
        switch (error) {
            case EGL14.EGL_SUCCESS:
                message = "EGL_SUCCESS";
                break;
            case EGL14.EGL_NOT_INITIALIZED:
                message = "EGL_NOT_INITIALIZED";
                break;
            case EGL14.EGL_BAD_ACCESS:
                message = "EGL_BAD_ACCESS";
                break;
            case EGL14.EGL_BAD_ALLOC:
                message = "EGL_BAD_ALLOC";
                break;
            case EGL14.EGL_BAD_ATTRIBUTE:
                message = "EGL_BAD_ATTRIBUTE";
                break;
            case EGL14.EGL_BAD_CONFIG:
                message = "EGL_BAD_CONFIG";
                break;
            case EGL14.EGL_BAD_CONTEXT:
                message = "EGL_BAD_CONTEXT";
                break;
            case EGL14.EGL_BAD_CURRENT_SURFACE:
                message = "EGL_BAD_CURRENT_SURFACE";
                break;
            case EGL14.EGL_BAD_DISPLAY:
                message = "EGL_BAD_DISPLAY";
                break;
            case EGL14.EGL_BAD_MATCH:
                message = "EGL_BAD_MATCH";
                break;
            case EGL14.EGL_BAD_NATIVE_PIXMAP:
                message = "EGL_BAD_NATIVE_PIXMAP";
                break;
            case EGL14.EGL_BAD_NATIVE_WINDOW:
                message = "EGL_BAD_NATIVE_WINDOW";
                break;
            case EGL14.EGL_BAD_PARAMETER:
                message = "EGL_BAD_PARAMETER";
                break;
            case EGL14.EGL_BAD_SURFACE:
                message = "EGL_BAD_SURFACE";
                break;
            case EGL14.EGL_CONTEXT_LOST:
                message = "EGL_CONTEXT_LOST";
                break;
            default:
                message = "EGL_UNKNOWN";
                break;
        }
        return message + " 0x" + Integer.toHexString(error);
    }
}
