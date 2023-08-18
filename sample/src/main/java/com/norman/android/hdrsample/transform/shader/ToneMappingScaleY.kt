package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object ToneMappingScaleY : GLShaderCode() {

    /*
        https://github.com/natural-harmonia-gropius/hdr-toys/blob/master/_dev/tone-mapping/linear_y.glsl
     */
    override val code: String
        get() = """
         #define L_hdr 1000.0
         #define L_sdr 203.0
         float curve(float x) {
             const float w = L_hdr / L_sdr;
             return x / w;
         }

         vec3 tone_mapping_y(vec3 RGB) {
             const float y = dot(RGB, vec3(0.2627002120112671, 0.6779980715188708, 0.05930171646986196));
             return RGB * curve(y) / y;
         }

         vec4 ${javaClass.name}(vec4 color) {
             color.rgb = tone_mapping_y(color.rgb);
             return color;
         }
        """.trimIndent()
}