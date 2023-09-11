package com.norman.android.hdrsample.transform.shader.gamma

/**
 * HLG的场景参考OETF
 * 参数详解见 https://juejin.cn/post/7231369710024310821#heading-8
 */

class HLGSceneOETF : GammaOETF() {

    val a = "${prefix}A"
    val b = "${prefix}B"
    val c = "${prefix}C"

    override val code: String
        get() = """
        #define  $a  0.17883277// ABC三个参数是为了平滑连接HLG的两端曲线
        #define  $b  0.28466892
        #define  $c  0.55991073

        vec3 $methodGamma(vec3 x)
        {
            return mix(sqrt(3.0 * x),
            $a * log(12.0 * x - $b) + $c,
            step(1.0 / 12.0, x));
        }
        """.trimIndent()

}
