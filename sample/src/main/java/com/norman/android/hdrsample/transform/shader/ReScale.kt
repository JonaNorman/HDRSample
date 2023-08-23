package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

/**
 * Gamma矫正后的颜色是归一化的，需要缩放成亮度绝对值，方便后续ToneMap后归一化
 */
abstract class ReScale : GLShaderCode() {
    val methodScaleAbsolute = ReScale.methodScaleAbsolute
    val methodScaleNormalize =  ReScale.methodScaleNormalize
    val methodNormalizeDisplay =  ReScale.methodNormalizeDisplay
    companion object {
        const val methodScaleAbsolute = "RESCALE_ABSOLUTE"
        const val methodScaleNormalize = "RESCALE_NORMALIZE"
        const val methodNormalizeDisplay = "NORMALIZE_DISPLAY"
    }
}