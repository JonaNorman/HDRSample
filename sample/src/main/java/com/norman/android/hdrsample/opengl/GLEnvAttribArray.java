package com.norman.android.hdrsample.opengl;

/***
 * EGL的属性列表
 */
interface GLEnvAttribArray {
    int getAttrib(int key);

    int[] getAttribArray();

    void setAttrib(int key, int value);
}
