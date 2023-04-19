package com.jonanorman.android.hdrsample.player.opengl.env;

import android.opengl.EGL14;

class EnvContextAttribImpl extends EnvAttribImpl implements GLEnvContextAttrib {

    @Override
    public void setClientVersion(@GLEnvContext.OpenGLESVersion int version) {
        put(EGL14.EGL_CONTEXT_CLIENT_VERSION, version);
    }
}
