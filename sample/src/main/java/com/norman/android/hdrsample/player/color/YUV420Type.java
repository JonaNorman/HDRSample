package com.norman.android.hdrsample.player.color;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({YUV420Type.YV21, YUV420Type.YV12, YUV420Type.NV12, YUV420Type.NV21})
@Retention(RetentionPolicy.SOURCE)
public @interface YUV420Type {
    int YV21 = 1;// Y+U+V
    int YV12 = 2;//Y+V+U
    int NV12 = 3;//Y+UV
    int NV21 = 4;//Y+VU
}
