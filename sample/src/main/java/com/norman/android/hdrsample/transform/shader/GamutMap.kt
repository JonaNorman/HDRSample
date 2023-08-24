package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

/**
 * 色域映射抽象类
 */
abstract class GamutMap: GLShaderCode() {

     val methodGamutMap = "GAMUT_MAP"

    companion object{
        @JvmField
        val  NONE = object: GamutMap() {
            override val code: String
                get() = """
                vec3 $methodGamutMap(vec3 color){
                    return color;
                }
                """.trimIndent()

        }

        @JvmField
        val  ADAPTIVE_L0_CUSP = GamutMapAdaptiveL0Cusp

        @JvmField
        val  CLIP = GamutMapClip

        @JvmField
        val  COMPRESS = GamutMapCompress
    }
}