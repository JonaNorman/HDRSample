package com.norman.android.hdrsample.transform.shader.gamma

/**
 * BT1886 EOTF公式，BT1886本身还有黑电平限制，黑电平带入0简化公式就变成如下pow(v,gamma)
 * 参考自https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.1886-0-201103-I!!PDF-E.pdf
 */
class BT1886EOTF : GammaEOTF() {
    private val gamma = "${prefix}GAMMA"
    override val code: String
        get() = """
        #define $gamma 2.4

        vec3 ${methodGamma}(vec3 V) {
            return pow(V, vec3($gamma));
        }
        """.trimIndent()
}