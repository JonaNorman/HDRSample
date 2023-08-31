package com.norman.android.hdrsample.player.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

class YUV420VertexShader :GLShaderCode(){

    override val code = """
        #version 300 es
        in vec4 $POSITION;
        in vec4 $INPUT_TEXTURE_COORDINATE;
        out vec2 textureCoordinate;
        void main() {
            gl_Position =$POSITION;
            textureCoordinate =$INPUT_TEXTURE_COORDINATE.xy;
        }
    """.trimIndent()

    companion object{
        @JvmField
        val POSITION = "position"

        @JvmField
        val INPUT_TEXTURE_COORDINATE = "inputTextureCoordinate"
    }

}
