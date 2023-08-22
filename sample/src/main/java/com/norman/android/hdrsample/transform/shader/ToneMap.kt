package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

/**
 * 色调映射抽象类
 */
abstract class ToneMap: GLShaderCode() {

    val methodToneMap = "TONE_MAP"
}