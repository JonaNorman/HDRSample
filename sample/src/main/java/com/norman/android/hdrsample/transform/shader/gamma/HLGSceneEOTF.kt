package com.norman.android.hdrsample.transform.shader.gamma

// HLG公式参数详解见 https://juejin.cn/post/7231369710024310821#heading-8
class HLGSceneEOTF : GammaOETF() {

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
            return mix(x * x / 3.0,
            (exp((x - $c) / $a) +$b) / 12.0,
            step(0.5, x));
        }
        """.trimIndent()

}
