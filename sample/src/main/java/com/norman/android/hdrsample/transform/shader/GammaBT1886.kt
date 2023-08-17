package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object GammaBT1886 : GLShaderCode() {
    override val code: String
        get() = """
            const float GAMMA = 2.4;
        const float L_W = 1.0;
        const float L_B = 0.0;

        // The reference EOTF specified in Rec. ITU-R BT.1886
        // L = a(max[(V+b),0])^g
        float bt1886_r(float L, float gamma, float Lw, float Lb) {
            float a = pow(pow(Lw, 1.0 / gamma) - pow(Lb, 1.0 / gamma), gamma);
            float b = pow(Lb, 1.0 / gamma) / (pow(Lw, 1.0 / gamma) - pow(Lb, 1.0 / gamma));
            float V = pow(max(L / a, 0.0), 1.0 / gamma) - b;
            return V;
        }

        vec4 color = HOOKED_tex(HOOKED_pos);
        vec4 hook() {
            color.rgb = vec3(
                bt1886_r(color.r, GAMMA, L_W, L_B),
                bt1886_r(color.g, GAMMA, L_W, L_B),
                bt1886_r(color.b, GAMMA, L_W, L_B)
            );
            return color;
        }

         float bt1886_eotf(float x, float min, float max)
        {
            const float lb = powf(min, 1/2.4f);
            const float lw = powf(max, 1/2.4f);
            return powf((lw - lb) * x + lb, 2.4f);
        }

         float bt1886_oetf(float x, float min, float max)
        {
            const float lb = powf(min, 1/2.4f);
            const float lw = powf(max, 1/2.4f);
            return (powf(x, 1/2.4f) - lb) / (lw - lb);
        }
        """.trimIndent()
}