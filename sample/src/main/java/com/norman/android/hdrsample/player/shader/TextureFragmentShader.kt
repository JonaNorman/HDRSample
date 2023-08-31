package com.norman.android.hdrsample.player.shader

import androidx.annotation.IntDef
import com.norman.android.hdrsample.opengl.GLShaderCode

class TextureFragmentShader(@TextureType textureType: Int) : GLShaderCode() {


    @JvmField
    val textureType: Int

    init {
        this.textureType = textureType
    }

    override val code: String
        get() = """
            |#version 300 es
            |${if (textureType != TYPE_TEXTURE_2D) "#extension GL_OES_EGL_image_external : require" else ""}
            |${if (textureType == TYPE_TEXTURE_Y2Y) "#extension GL_EXT_YUV_target : require" else ""}
            |precision highp float;
            |in vec2 textureCoordinate;
            |out vec4 outColor;
            |uniform ${
            if (textureType == TYPE_TEXTURE_2D) "sampler2D"
            else if (textureType == TYPE_TEXTURE_OES) "samplerExternalOES"
            else "__samplerExternal2DY2YEXT"
        } $INPUT_IMAGE_TEXTURE;
            |${if (textureType == TYPE_TEXTURE_Y2Y) "uniform mat4 $Y2Y_TO_RGB_MATRIX;" else ""}
            |void main()
            |{
            |    vec4 color = texture($INPUT_IMAGE_TEXTURE, textureCoordinate);
            |    vec3 rgb = color.rgb;
            |    ${if (textureType == TYPE_TEXTURE_Y2Y) "rgb = ($Y2Y_TO_RGB_MATRIX *vec4(rgb, 1.0)).rgb;" else ""}
            |    outColor.rgb =rgb;
            |    outColor.a= color.a;
            |}
            """.trimMargin()

    companion object {
        const val TYPE_TEXTURE_2D = 1

        const val TYPE_TEXTURE_OES = 2

        const val TYPE_TEXTURE_Y2Y = 3

        const val Y2Y_TO_RGB_MATRIX = "Y2Y_TO_RGB_MATRIX"

        const val INPUT_IMAGE_TEXTURE = "inputImageTexture"
    }

    @IntDef(TYPE_TEXTURE_2D, TYPE_TEXTURE_OES, TYPE_TEXTURE_Y2Y)
    @Retention(AnnotationRetention.SOURCE)
    annotation class TextureType
}