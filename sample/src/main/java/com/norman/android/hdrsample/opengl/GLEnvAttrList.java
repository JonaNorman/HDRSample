package com.norman.android.hdrsample.opengl;

/***
 * EGL的属性列表， 实现这个类的目的是为了使用EGL不需要拼装数组
 */
interface GLEnvAttrList {
    int getAttrib(int key);

    int[] getAttribArray();

    void setAttrib(int key, int value);
}
