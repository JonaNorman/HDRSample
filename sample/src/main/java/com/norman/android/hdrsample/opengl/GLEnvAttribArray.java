package com.norman.android.hdrsample.opengl;

interface GLEnvAttribArray {
    int getAttrib(int key);

    int[] getAttribArray();

    void setAttrib(int key, int value);
}
