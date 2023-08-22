package com.norman.android.hdrsample.transform.shader.todo

import com.norman.android.hdrsample.opengl.GLShaderCode

object ToneMappingScaleRGB : GLShaderCode() {

    /*
        https://github.com/natural-harmonia-gropius/hdr-toys/blob/master/_dev/tone-mapping/linear_rgb.glsl
     */
    override val code: String
        get() = """
            #define L_hdr 1000.0
            #define L_sdr 203.0

            float curve(float x) {
                const float w = L_hdr / L_sdr;
                return x / w;
            }

            vec3 tone_mapping_rgb(vec3 RGB) {
                return vec3(curve(RGB.r), curve(RGB.g), curve(RGB.b));
            }

            vec4 ${javaClass.name}(vec4 color) {
                color.rgb = tone_mapping_rgb(color.rgb);

                return color;
            }
        """.trimIndent()
}