package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

/**
 * 色度矫正抽象类
 */
abstract class ChromaCorrection: GLShaderCode() {

    val methodChromaCorrect = "CHROMA_CORRECT"

    object NONE : ChromaCorrection() {
        override val code: String
            get() = """
                vec3 $methodChromaCorrect(vec3 color){
                    return color;
                }
            """.trimIndent()

    }
}