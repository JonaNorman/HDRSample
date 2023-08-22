package com.norman.android.hdrsample.transform.shader.todo

import com.norman.android.hdrsample.opengl.GLShaderCode

object ToneMapMaxRGB : GLShaderCode() {

    /*
        https://github.com/natural-harmonia-gropius/hdr-toys/blob/master/_dev/tone-mapping/linear_oklab.glsl
     */
    override val code: String
        get() = """
            #define L_hdr 1000.0
            #define L_sdr 203.0
            float curve(float x) {
                const float w = L_hdr / L_sdr;
                return x / w;
            }

            vec3 tone_mapping_max(vec3 RGB) {
                const float m = max(max(RGB.r, RGB.g), RGB.b);
                return RGB * curve(m) / m;
            }

            vec4 hook() {
                vec4 color = HOOKED_texOff(0);

                color.rgb = tone_mapping_max(color.rgb);

                return color;
            }
        """.trimIndent()
}