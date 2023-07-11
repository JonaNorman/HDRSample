package com.norman.android.hdrsample.opengl;

import androidx.annotation.NonNull;

class EnvWindowSurfaceAttribImpl extends EnvSurfaceAttrsImpl implements GLEnvWindowSurfaceAttribArray {

    @NonNull
    @Override
    public EnvWindowSurfaceAttribImpl clone() {
        return (EnvWindowSurfaceAttribImpl) super.clone();
    }
}
