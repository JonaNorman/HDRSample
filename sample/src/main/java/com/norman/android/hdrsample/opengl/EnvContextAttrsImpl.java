package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;

class EnvContextAttrsImpl extends EnvAttrsImpl implements GLEnvContextAttrs {

    @Override
    public void setClientVersion(@GLEnvContext.OpenGLESVersion int version) {
        setAttrib(EGL14.EGL_CONTEXT_CLIENT_VERSION, version);
    }
}
