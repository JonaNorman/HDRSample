package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object ConstantParams : GLShaderCode() {
    const val PI ="PI"
    const val EPSILON ="EPSILON"//精度判断
    const val HDR_REFERENCE_WHITE ="HDR_REFERENCE_WHITE"
    const val HLG_MAX_LUMINANCE ="HLG_MAX_LUMINANCE"
    const val PQ_MAX_LUMINANCE ="PQ_MAX_LUMINANCE"

    override val code: String
        get() = """
            #define  $PI  3.1415926 //圆周率
            #define  $EPSILON  1e-6 // 精度阙值
            #define  $HDR_REFERENCE_WHITE 203.0;  //HDR参考白亮度
            #define  $HLG_MAX_LUMINANCE 1000.0;  //HLG最大亮度
            #define  $PQ_MAX_LUMINANCE 10000.0;  //PQ最大亮度
        """.trimIndent()
 }
