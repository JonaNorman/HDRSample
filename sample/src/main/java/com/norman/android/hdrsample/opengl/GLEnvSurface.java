package com.norman.android.hdrsample.opengl;


import android.opengl.EGLSurface;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface GLEnvSurface {


    int COLOR_SPACE_SRGB = 1;
    int COLOR_SPACE_LINEAR = 2;
    int COLOR_SPACE_BT2020_PQ = 3;
    int COLOR_SPACE_DISPLAY_P3 = 4;
    int COLOR_SPACE_DISPLAY_P3_PASSTHROUGH = 5;


    @IntDef({COLOR_SPACE_SRGB, COLOR_SPACE_LINEAR, COLOR_SPACE_BT2020_PQ, COLOR_SPACE_DISPLAY_P3, COLOR_SPACE_DISPLAY_P3_PASSTHROUGH})
    @Retention(RetentionPolicy.SOURCE)
    @interface ColorSpace {
    }

    GLEnvDisplay getEnvDisplay();

    GLEnvConfig getEnvConfig();

    EGLSurface getEGLSurface();


    int getHeight();

    int getWidth();

    void release();

    boolean isRelease();

}
