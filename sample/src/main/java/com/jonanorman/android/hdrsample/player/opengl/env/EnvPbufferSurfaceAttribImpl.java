package com.jonanorman.android.hdrsample.player.opengl.env;

import android.opengl.EGL14;

class EnvPbufferSurfaceAttribImpl extends EnvSurfaceAttribImpl implements GLEnvPbufferSurfaceAttrib {

    int width;
    int height;

    public void setWidth(int width) {
        put(EGL14.EGL_WIDTH, width);
        this.width = width;
    }

    public void setHeight(int height) {
        put(EGL14.EGL_HEIGHT, height);
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
