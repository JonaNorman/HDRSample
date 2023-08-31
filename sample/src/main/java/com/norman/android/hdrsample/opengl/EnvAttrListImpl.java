package com.norman.android.hdrsample.opengl;

import android.opengl.EGL14;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;

/**
 * EGL的属性列表实现类，不需要拼装数组
 */
class EnvAttrListImpl implements GLEnvAttrList,Cloneable {
    private  SparseIntArray attributeArr = new SparseIntArray();
    /**
     * 数据是否要求更新true表示getAttribArray时候会重新更新数据
     */
    private boolean requireUpdate = true;
    private int[] attrib;

    @Override
    public void setAttrib(int key, int value) {
        attributeArr.put(key, value);
        requireUpdate = true;
    }

    @Override
    public int getAttrib(int key) {
        return attributeArr.get(key);
    }

    /**
     * EGL的属性列表拼装规则要求key，value一对，最后加上EGL14.EGL_NONE结尾
     * @return
     */
    @Override
    public int[] getAttribArray() {
        if (requireUpdate) {
            int length = attributeArr.size() * 2 + 1;
            attrib = new int[length];
            for (int i = 0; i < attributeArr.size(); i++) {
                attrib[i*2] = attributeArr.keyAt(i);
                attrib[i*2 + 1] = attributeArr.valueAt(i);
            }
            attrib[length - 1] = EGL14.EGL_NONE;
            requireUpdate = false;
        }
        return attrib;
    }

    @NonNull
    @Override
    public EnvAttrListImpl clone() {
        EnvAttrListImpl clone;
        try {
            clone = (EnvAttrListImpl) super.clone();
            clone.attributeArr = attributeArr.clone();
            clone.requireUpdate = true;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
        return clone;
    }
}
