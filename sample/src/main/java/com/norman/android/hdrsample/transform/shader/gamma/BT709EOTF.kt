package com.norman.android.hdrsample.transform.shader.gamma

/**
 * BT709的Gamma公式参数详解见 https://juejin.cn/post/7231369710024310821#heading-5
 */
class BT709EOTF : GammaEOTF() {
    private val alpha = "${prefix}ALPHA"
    private val beta = "${prefix}BETA"
    private val gamma = "${prefix}GAMMA"
    override val code: String
        get() = """
        #define $alpha   1.09929682680944//ALPHA和BETA是为了平滑直线和曲线算出来的
        #define $beta    0.018053968510807
        #define $gamma   1.0/0.45
        
        vec3 ${methodGamma}(vec3 x)
        {
            return mix(x / 4.5, 
            pow((x + ($alpha - 1.0)) / $alpha, 
            vec3($gamma)), step($beta*4.5, x));
        }
        """.trimIndent()
}