package com.norman.android.hdrsample.transform.shader.gamma

// BT709 OETF公式
// 详解见 https://juejin.cn/post/7231369710024310821#heading-5
class BT709OETF : GammaOETF() {
    private val alpha = "${prefix}ALPHA"
    private val beta = "${prefix}BETA"
    private val gammaInverse = "${prefix}GAMMA_INVERSE"
    override val code: String
        get() = """
        #define $alpha    1.09929682680944//ALPHA和BETA是为了平滑直线和曲线算出来的
        #define $beta     0.018053968510807
        #define $gammaInverse    0.45

        vec3 ${methodGamma}(vec3 x)
        {
            return mix(x * 4.5, 
            $alpha * pow(x, vec3($gammaInverse)) - ($alpha - 1.0), 
            step($beta, x));
        }
        """.trimIndent()
}