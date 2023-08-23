package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

/**
 * Gamma矫正后的颜色是归一化的，需要缩放成亮度绝对值，方便后续ToneMap后归一化
 */
abstract class ReScale : GLShaderCode() {

    val methodScale = "RESCALE_IN"
    val methodNormalize = "RESCALE_OUT"
}