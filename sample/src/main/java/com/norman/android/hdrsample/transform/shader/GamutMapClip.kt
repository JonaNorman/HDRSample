package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object GamutMapClip: GLShaderCode() {
    /**
     * 直接裁剪BT2020转BT709后超出范围的值
     */
    override val code: String
        get() = """
        vec4 ${javaClass.name}(vec4 color) {
            vec3 color_src = color.rgb;
            vec3 color_dst = BT2020_TO_BT709(color_src);
            vec3 color_dst_cliped = clamp(color_dst, 0.0, 1.0);
            color.rgb = color_dst_cliped;
            return color;
        }
        """.trimIndent()
}