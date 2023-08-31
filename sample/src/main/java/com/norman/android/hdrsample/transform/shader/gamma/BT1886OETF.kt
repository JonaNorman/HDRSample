package com.norman.android.hdrsample.transform.shader.gamma

// 参考自https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.1886-0-201103-I!!PDF-E.pdf
class BT1886OETF : GammaOETF() {

    private val gammaInverse = "${prefix}GAMMA_INVERSE"
    override val code: String
        get() = """
   
        #define $gammaInverse 1.0/2.4
        vec3 ${methodGamma}(vec3 L) {
            return pow(L, vec3($gammaInverse));
        }
        """.trimIndent()
}