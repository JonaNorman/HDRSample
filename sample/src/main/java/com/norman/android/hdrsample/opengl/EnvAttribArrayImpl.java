package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;

class EnvAttribArrayImpl implements GLEnvAttribArray ,Cloneable {
    private  SparseIntArray attributeArr = new SparseIntArray();
    private boolean update = true;
    private int[] attrib;

    @Override
    public void setAttrib(int key, int value) {
        attributeArr.put(key, value);
        update = true;
    }

    @Override
    public int getAttrib(int key) {
        return attributeArr.get(key);
    }

    @Override
    public int[] getAttribArray() {
        if (update) {
            int length = attributeArr.size() * 2 + 1;
            attrib = new int[length];
            for (int i = 0; i < attributeArr.size(); i++) {
                attrib[i] = attributeArr.keyAt(i);
                attrib[i + 1] = attributeArr.valueAt(i);
            }
            attrib[length - 1] = EGL14.EGL_NONE;
            update = false;
        }
        return attrib;
    }

    @NonNull
    @Override
    public EnvAttribArrayImpl clone() {
        EnvAttribArrayImpl clone;
        try {
            clone = (EnvAttribArrayImpl) super.clone();
            clone.attributeArr = attributeArr.clone();
            clone.update = true;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
        return clone;
    }
}
