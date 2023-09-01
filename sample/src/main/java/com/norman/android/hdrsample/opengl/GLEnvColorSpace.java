package com.norman.android.hdrsample.opengl;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * EGLSurface的颜色空间
 */

@IntDef({GLEnvColorSpace.SRGB, GLEnvColorSpace.LINEAR, GLEnvColorSpace.BT2020_PQ, GLEnvColorSpace.BT2020_HLG, GLEnvColorSpace.BT2020_LINEAR, GLEnvColorSpace.DISPLAY_P3, GLEnvColorSpace.DISPLAY_P3_PASSTHROUGH})
@Retention(RetentionPolicy.SOURCE)
public @interface GLEnvColorSpace {
    int SRGB = 1;
    int LINEAR = 2;
    int BT2020_PQ = 3;
    int BT2020_HLG = 4;
    int BT2020_LINEAR = 5;
    int DISPLAY_P3 = 6;
    int DISPLAY_P3_PASSTHROUGH = 7;
}
