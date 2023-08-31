package com.norman.android.hdrsample.transform.shader.gamma

import com.norman.android.hdrsample.opengl.GLShaderCode

abstract class GammaFunction : GLShaderCode() {
    val prefix :String
        get() {
            return javaClass.simpleName+hashCode()+"_"
        }
    val methodGamma :String
        get() {
            return prefix+"gammaMethod"
        }
}