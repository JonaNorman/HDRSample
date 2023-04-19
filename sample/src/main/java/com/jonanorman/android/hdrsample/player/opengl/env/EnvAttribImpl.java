package com.jonanorman.android.hdrsample.player.opengl.env;

import android.opengl.EGL14;
import android.util.SparseArray;

class EnvAttribImpl implements GLEnvAttrib {
    private final SparseArray<Integer> attributeArr = new SparseArray<>();
    private boolean update = true;
    private int[] attrib;

    @Override
    public void put(int key, int value) {
        attributeArr.put(key, value);
        update = true;
    }

    @Override
    public int[] getAttrib() {
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
