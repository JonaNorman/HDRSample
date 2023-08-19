package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode
import com.norman.android.hdrsample.transform.shader.ColorSpaceConversion.methodBt2020ToBt709

/**
 * 直接裁剪BT2020转BT709色域映射后超出范围的值
 */
object GamutMapClip: GLShaderCode() {

    init {
        includeList.add(ColorSpaceConversion)
    }
    override val code: String
        get() = """
        vec4 ${javaClass.name}(vec4 color) {
            vec3 rgb = color.rgb;
            rgb = $methodBt2020ToBt709(rgb);
            color.rgb = clamp(rgb, 0.0, 1.0);
            return color;
        }
        """.trimIndent()
}