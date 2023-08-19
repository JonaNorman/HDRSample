package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object HDRParams : GLShaderCode() {
    const val paramHdrMaxLuminance ="HDR_MAX_LUMINANCE"
    const val paramHdrReferenceWhite ="HDR_REFERENCE_WHITE"

    override val code: String
        get() = """
            uniform float $paramHdrMaxLuminance=1000.0; //HDR亮度最大值
            uniform float $paramHdrReferenceWhite=203.0;  //HDR参考白亮度
          
            
        """.trimIndent()
 }
