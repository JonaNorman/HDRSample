package com.norman.android.hdrsample.player.shader

import androidx.annotation.IntDef
import com.norman.android.hdrsample.opengl.GLShaderCode

/**
 * 纹理FragmentShader
 * 支持3种格式 2D OES Y2Y渲染到frameBuffer上
 * 2D纹理格式需要sampler2D+texture配置
 * OES纹理格式需要samplerExternalOES+GL_OES_EGL_image_external扩展配置
 * OES纹理格式可以关联在SurfaceTexture，从而做到把SurfaceTexture的内容渲染到frameBuffer上
 * Y2Y纹理格式在OES纹理格式的基础上需要GL_EXT_YUV_target+__samplerExternal2DY2YEXT支持
 * Y2Y纹理输出的颜色是YUV需要转成RGB，Y2Y纹理转换的RGB颜色比OES纹理的颜色色域可控，色差更准，据说支持的位数比OES纹理大(不确定)
 * 可以确认的是之所以叫Y2Y是因为直接把YUV的内容渲染到YUV的Surface上不用中转，这里我们没有使用YUV的Surface所以还需要转成RGB
 */
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