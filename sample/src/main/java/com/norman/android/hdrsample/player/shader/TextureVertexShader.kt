package com.norman.android.hdrsample.player.shader

import androidx.annotation.IntDef
import com.norman.android.hdrsample.opengl.GLShaderCode

/**
 * 纹理VertexShader
 */
class TextureVertexShader : GLShaderCode() {


    override val code = """
            #version 300 es
            precision highp float;
            in vec4 $POSITION;
            in vec4 $INPUT_TEXTURE_COORDINATE;
            uniform mat4 $TEXTURE_MATRIX;
            out vec2 textureCoordinate;
            void main() {
                gl_Position =position;
                textureCoordinate =($TEXTURE_MATRIX*$INPUT_TEXTURE_COORDINATE).xy;
            }
            """.trimIndent()

    companion object {
        @JvmField
        val POSITION = "position"

        @JvmField
        val INPUT_TEXTURE_COORDINATE = "inputTextureCoordinate"

        @JvmField
        val TEXTURE_MATRIX = "textureMatrix"

    }

}