package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;

import androidx.annotation.NonNull;

class EnvPbufferSurfaceAttribArrayImpl extends EnvSurfaceAttrsImpl implements GLEnvPbufferSurfaceAttribArray {


    public void setWidth(int width) {
        setAttrib(EGL14.EGL_WIDTH, width);
    }

    public void setHeight(int height) {
        setAttrib(EGL14.EGL_HEIGHT, height);
    }

    @Override
    public int getWidth() {
        return getAttrib(EGL14.EGL_WIDTH);
    }

    @Override
    public int getHeight() {
        return getAttrib(EGL14.EGL_HEIGHT);
    }

    @NonNull
    @Override
    public EnvPbufferSurfaceAttribArrayImpl clone() {
        return (EnvPbufferSurfaceAttribArrayImpl) super.clone();
    }
}
