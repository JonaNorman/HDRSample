package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;
import android.util.SparseArray;

class EnvAttribArrayImpl implements GLEnvAttribArray {
    private final SparseArray<Integer> attributeArr = new SparseArray<>();
    private boolean update = true;
    private int[] attrib;

    @Override
    public void setAttrib(int key, int value) {
        attributeArr.put(key, value);
        update = true;
    }

    @Override
    public int getAttrib(int key) {
        Integer value = attributeArr.get(key);
        return value != null ? value : 0;
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
}
