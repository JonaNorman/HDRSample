package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object TransferPQ: GLShaderCode() {
    override val code: String
        get() = """
        vec4 ${javaClass.name}(vec4 color) {
            return color;
        }
        """.trimIndent()
}