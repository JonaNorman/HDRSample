package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object ToneMappingHable : GLShaderCode() {
    // Filmic curve by John Hable, Also known as the "Uncharted 2 curve".
    // http://filmicworlds.com/blog/filmic-tonemapping-operators/
    override val code: String
        get() = """
            #define L_hdr 1000.0
            #define L_sdr 203.0
            const float A = 0.15;   // Shoulder Strength
            const float B = 0.50;   // Linear Strength
            const float C = 0.10;   // Linear Angle
            const float D = 0.20;   // Toe Strength
            const float E = 0.02;   // Toe Numerator
            const float F = 0.30;   // Toe Denominator

            float f(float x) {
                return ((x * (A * x + C * B) + D * E) / (x * (A * x + B) + D * F)) - E / F;
            }

            float curve(float x) {
                const float W = L_hdr / L_sdr;
                return f(x) / f(W);
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