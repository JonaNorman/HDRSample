package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

/**
 * 色调映射抽象类
 */
abstract class ToneMap: GLShaderCode() {

    val methodToneMap = "TONE_MAP"

    companion object{
        @JvmField
        val  NONE = object: ToneMap() {
            override val code: String
                get() = """
                vec3 $methodToneMap(vec3 color){
                    return color;
                }
                """.trimIndent()

        }
    }


}