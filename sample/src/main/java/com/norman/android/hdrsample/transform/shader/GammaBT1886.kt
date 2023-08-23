package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

// 参考自https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.1886-0-201103-I!!PDF-E.pdf
object GammaBT1886 : GLShaderCode() {

    const val methodBT1886EOTF = "BT1886_EOTF"
    const val methodBT1886OETF = "BT1886_OETF"

    override val code: String
        get() = """
        #define BT1886_GAMMA 2.4
        #define BT1886_GAMMA_INV 1.0/BT1886_GAMMA
        #define BT1886_LW    1.0
        #define BT1886_LB    0.0
        #define BT18886_a   (pow(pow(BT1886_LW, BT1886_GAMMA_INV) - pow(BT1886_LB, BT1886_GAMMA_INV), BT1886_GAMMA))
        #define BT18886_b   (pow(BT1886_LB, BT1886_GAMMA_INV) / (pow(BT1886_LW, BT1886_GAMMA_INV) - pow(BT1886_LB, BT1886_GAMMA_INV)))


        float $methodBT1886OETF(float L) {
            float a = BT18886_a;
            float b = BT18886_b;
            float V = pow(max(L / a, 0.0), BT1886_GAMMA_INV) - b;
            return V;
        }

        // L = a(max[(V+b),0])^g
        float $methodBT1886EOTF(float V) {
            float a = BT18886_a;
            float b = BT18886_b;
            float L = a * pow(max(V + b, 0.0), BT1886_GAMMA);
            return L;
        }
        
        """.trimIndent()
}