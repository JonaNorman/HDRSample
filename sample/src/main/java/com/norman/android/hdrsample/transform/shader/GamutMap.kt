package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode
import com.norman.android.hdrsample.transform.shader.ColorSpaceConversion.methodBt2020ToBt709

/**
 * 直接裁剪BT2020转BT709色域映射后超出范围的值
 */
abstract class GamutMap: GLShaderCode() {

    val methodGamutMap = "GAMUT_MAP"
}