package com.jonanorman.android.hdrsample.player.opengl.env;

import android.opengl.EGLDisplay;

public interface GLEnvDisplay {

    static GLEnvDisplay createDisplay() {
        return new EnvDisplayImpl();
    }

    static GLEnvDisplay createDisplay(int displayId) {
        return new EnvDisplayImpl(displayId);
    }

    EGLDisplay getEGLDisplay();

    int getDisplayId();

    GLEnvConfig chooseConfig(GLEnvConfigChooser configChooser);

    void release();

    boolean isSupportBT2020PQ();

    boolean isSupportSurfacelessContext();

    String getEGLExtensions();

    boolean isRelease();

}
