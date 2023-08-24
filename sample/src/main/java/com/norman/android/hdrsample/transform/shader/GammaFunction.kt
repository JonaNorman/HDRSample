package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

abstract class GammaFunction : GLShaderCode() {
    abstract val methodOETF: String
    abstract val methodEOTF: String

}