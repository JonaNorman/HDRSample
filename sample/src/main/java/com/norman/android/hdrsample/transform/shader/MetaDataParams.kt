package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode
import com.norman.android.hdrsample.player.VideoOutput

object MetaDataParams : GLShaderCode() {
    const val MIN_DISPLAY_LUMINANCE ="MIN_DISPLAY_LUMINANCE"// 最小屏幕亮度
    const val MAX_DISPLAY_LUMINANCE ="MAX_DISPLAY_LUMINANCE"// 最大屏幕亮度
    const val CURRENT_DISPLAY_LUMINANCE ="CURRENT_DISPLAY_LUMINANCE"// 当前亮度
    const val MAX_FRAME_AVERAGE_LUMINANCE ="MAX_FRAME_AVERAGE_LUMINANCE"// 最大平均亮度
    const val MAX_CONTENT_LUMINANCE ="MAX_CONTENT_LUMINANCE"// 最大平均亮度
    const val COLOR_SPACE ="COLOR_SPACE"//
    const val COLOR_SPACE_BT2020_PQ ="COLOR_SPACE_BT2020_PQ"//
    const val COLOR_SPACE_BT2020_HLG ="COLOR_SPACE_BT2020_HLG"//
    const val PI ="PI"
    const val EPSILON ="EPSILON"//精度判断
    const val HDR_REFERENCE_WHITE ="HDR_REFERENCE_WHITE"
    const val HLG_MAX_LUMINANCE ="HLG_MAX_LUMINANCE"
    const val PQ_MAX_LUMINANCE ="PQ_MAX_LUMINANCE"
    override val code: String
        get() = """
              uniform float $MIN_DISPLAY_LUMINANCE;// 最大屏幕亮度
              uniform float $MAX_DISPLAY_LUMINANCE;// 最大屏幕亮度
              uniform float $CURRENT_DISPLAY_LUMINANCE;// 当前亮度
              uniform float $MAX_FRAME_AVERAGE_LUMINANCE;// 最大平均亮度
              uniform float $MAX_CONTENT_LUMINANCE;// 最大亮度
              uniform int $COLOR_SPACE;// 最大平均亮度
              #define $COLOR_SPACE_BT2020_PQ ${VideoOutput.COLOR_SPACE_BT2020_PQ}
              #define $COLOR_SPACE_BT2020_HLG ${VideoOutput.COLOR_SPACE_BT2020_HLG}
              #define  $PI  3.1415926 //圆周率
              #define  $EPSILON  1e-6 // 精度阙值
              #define  $HDR_REFERENCE_WHITE 203.0;  //HDR参考白亮度
              #define  $HLG_MAX_LUMINANCE 1000.0;  //HLG最大亮度
              #define  $PQ_MAX_LUMINANCE 10000.0;  //PQ最大亮度
     
        """.trimIndent()
 }
