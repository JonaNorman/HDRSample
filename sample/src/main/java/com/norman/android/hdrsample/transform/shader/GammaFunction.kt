package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

abstract class GammaFunction : GLShaderCode() {
    abstract val methodOETF: String
    abstract val methodEOTF: String


    companion object {
        @JvmField
        val LINEAR = GammaLinear

        @JvmField
        val BT1886 = GammaBT1886

        @JvmField
        val BT709 = GammaBT709

        @JvmField
        val HLG = GammaHLG

        @JvmField
        val PQ = GammaPQ
    }

}