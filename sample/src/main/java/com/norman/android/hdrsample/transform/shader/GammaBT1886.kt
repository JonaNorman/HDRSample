package com.norman.android.hdrsample.transform.shader

// 参考自https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.1886-0-201103-I!!PDF-E.pdf
object GammaBT1886 : GammaFunction() {

    override val methodOETF = "BT1886_OETF"
    override val methodEOTF = "BT1886_EOTF"

    override val code: String
        get() = """
        #define BT1886_GAMMA 2.4
        #define BT1886_GAMMA_INV 1.0/BT1886_GAMMA
        #define BT1886_LW    1.0
        #define BT1886_LB    0.0
        #define BT18886_a   (pow(pow(BT1886_LW, BT1886_GAMMA_INV) - pow(BT1886_LB, BT1886_GAMMA_INV), BT1886_GAMMA))
        #define BT18886_b   (pow(BT1886_LB, BT1886_GAMMA_INV) / (pow(BT1886_LW, BT1886_GAMMA_INV) - pow(BT1886_LB, BT1886_GAMMA_INV)))


        vec3 $methodOETF(vec3 L) {
            float a = BT18886_a;
            float b = BT18886_b;
            vec3 V = pow(max(L / a, vec3(0.0)), vec3(BT1886_GAMMA_INV)) - b;
            return V;
        }

        // L = a(max[(V+b),0])^g
        vec3 $methodEOTF(vec3 V) {
            float a = BT18886_a;
            float b = BT18886_b;
            vec3 L = a * pow(max(V + b, vec3(0.0)), vec3(BT1886_GAMMA));
            return L;
        }
        """.trimIndent()
}