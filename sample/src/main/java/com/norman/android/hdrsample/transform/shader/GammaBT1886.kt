package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object GammaBT1886 : GLShaderCode() {

    // 参考自https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.1886-0-201103-I!!PDF-E.pdf
    override val code: String
        get() = """
        #define BT1886_GAMMA 2.4
        #define BT1886_GAMMA_INV 1.0/BT1886_GAMMA
        #define BT1886_LW    1.0
        #define BT1886_LB    0.0
        // L = a(max[(V+b),0])^g
        float BT1886_EOTF(float L) {
            float a = pow(pow(Lw, BT1886_GAMMA_INV) - pow(Lb, BT1886_GAMMA_INV), BT1886_GAMMA);
            float b = pow(Lb, BT1886_GAMMA_INV) / (pow(Lw, BT1886_GAMMA_INV) - pow(Lb, BT1886_GAMMA_INV));
            float V = pow(max(L / a, 0.0), BT1886_GAMMA_INV) - b;
            return V;
        }
        """.trimIndent()
}