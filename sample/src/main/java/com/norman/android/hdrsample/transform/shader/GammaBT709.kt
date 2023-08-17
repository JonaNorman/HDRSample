package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object GammaBT709 : GLShaderCode() {
    // BT709的Gamma公式参数详解见 https://juejin.cn/post/7231369710024310821#heading-5
    override val code: String
        get() = """
        #define BT709_ALPHA    1.09929682680944//ALPHA和BETA是为了平滑直线和曲线算出来的
        #define BT709_BETA     0.018053968510807
        #define BT709_GAMMA    0.45

        vec3 BT709_OETF(vec3 x)
        {
            return mix(x * 4.5, BT709_ALPHA * pow(x, vec3(BT709_GAMMA)) - (BT709_ALPHA - 1.0), step(BT709_BETA, x));
        }
        """.trimIndent()
}