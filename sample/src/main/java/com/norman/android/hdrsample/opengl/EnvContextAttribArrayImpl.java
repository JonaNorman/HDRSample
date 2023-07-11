package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;

import androidx.annotation.NonNull;

class EnvContextAttribArrayImpl extends EnvAttribArrayImpl implements GLEnvContextAttribArray {

    @Override
    public void setClientVersion(@GLEnvContext.OpenGLESVersion int version) {
        setAttrib(EGL14.EGL_CONTEXT_CLIENT_VERSION, version);
    }

    @NonNull
    @Override
    public EnvContextAttribArrayImpl clone() {
        return (EnvContextAttribArrayImpl) super.clone();
    }
}
