package com.norman.android.hdrsample.transform.shader.tonemap

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

        @JvmField
        val  ANDROID8 = ToneMapAndroid8

        @JvmField
        val  ANDROID13 = ToneMapAndroid13

        @JvmField
        val  BT2446A = ToneMapBT2446A

        @JvmField
        val  BT2446C = ToneMapBT2446C


        @JvmField
        val  HABLE = ToneMappingHable
    }


}