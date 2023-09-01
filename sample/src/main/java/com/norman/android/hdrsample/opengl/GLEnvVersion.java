package com.norman.android.hdrsample.opengl;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({GLEnvVersion.VERSION_1, GLEnvVersion.VERSION_2, GLEnvVersion.VERSION_3})
@Retention(RetentionPolicy.SOURCE)
public @interface GLEnvVersion {
    int VERSION_1 = 1;
    int VERSION_2 = 2;
    int VERSION_3 = 3;
}
