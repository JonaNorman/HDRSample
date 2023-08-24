package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

/**
 * 色度矫正抽象类
 */
abstract class ChromaCorrection : GLShaderCode() {

    val methodChromaCorrect = "CHROMA_CORRECT"

    companion object {
        @JvmField
        val NONE = object : ChromaCorrection() {
            override val code: String
                get() = """
                vec3 $methodChromaCorrect(vec3 color){
                    return color;
                }
                """.trimIndent()

        }

        @JvmField
        val BT2446C = ChromaCorrectionBT2446C
    }
}