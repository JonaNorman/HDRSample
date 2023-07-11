package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;

class EnvPbufferSurfaceAttribArrayImpl extends EnvSurfaceAttrsImpl implements GLEnvPbufferSurfaceAttribArray {

    int width;
    int height;

    public void setWidth(int width) {
        setAttrib(EGL14.EGL_WIDTH, width);
        this.width = width;
    }

    public void setHeight(int height) {
        setAttrib(EGL14.EGL_HEIGHT, height);
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

}
