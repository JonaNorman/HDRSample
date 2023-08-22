package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode
import com.norman.android.hdrsample.transform.shader.ColorSpaceConversion.methodBt2020ToBt709

/**
 * 色调映射抽象类
 */
abstract class ToneMap: GLShaderCode() {

    val methodToneMap = "TONE_MAP"
}