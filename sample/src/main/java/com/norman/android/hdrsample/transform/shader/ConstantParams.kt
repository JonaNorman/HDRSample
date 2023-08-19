package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object ConstantParams : GLShaderCode() {
    const val PI ="PI"
    const val EPSILON ="EPSILON"//精度判断

    override val code: String
        get() = """
            #define  $PI  3.1415926
            #define  $EPSILON  1e-6
        """.trimIndent()
 }
