package com.norman.android.hdrsample.transform.shader.gamma

/**
 *  S170M和BT709很像
 *  https://www.itu.int/rec/R-REC-BT.1700-0-200502-I/en
 */
class S170MEOTF : GammaEOTF() {
    val gamma = "${prefix}GAMMA"

    override val code: String
        get() = """
        #define $gamma    1.0/0.45
        
        vec3 $methodGamma(vec3 x)
        {
            return mix(x / 4.5, 
            pow((x+0.099)/1.099,vec3($gamma)), 
            step(0.0812, x));
        }
        """.trimIndent()
}