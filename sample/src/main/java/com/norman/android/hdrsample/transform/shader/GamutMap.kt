package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

/**
 * 色域映射抽象类
 */
abstract class GamutMap: GLShaderCode() {

    val methodGamutMap = "GAMUT_MAP"
}