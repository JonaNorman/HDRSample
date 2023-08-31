package com.norman.android.hdrsample.transform.shader.gamma

/**
 * S170M和BT709很像
 * https://www.itu.int/rec/R-REC-BT.1700-0-200502-I/en
 */
class S170MOETF : GammaOETF() {

    val gammaInverse = "${prefix}GAMMA_INVERSE"
    override val code: String
        get() = """
        #define $gammaInverse 0.45

        vec3 $methodGamma(vec3 x)
        {
           return mix(x * 4.5, 
           1.099*pow(x,vec3($gammaInverse))-0.099, 
           step(0.018, x));
        }
        """.trimIndent()
}