package com.norman.android.hdrsample.opengl;

/***
 * EGL的属性列表， 不需要拼装数组
 */
interface GLEnvAttrList {
    int getAttrib(int key);

    int[] getAttribArray();

    void setAttrib(int key, int value);
}
