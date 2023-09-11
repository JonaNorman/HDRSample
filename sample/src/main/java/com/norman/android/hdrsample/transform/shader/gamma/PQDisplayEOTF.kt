package com.norman.android.hdrsample.transform.shader.gamma

/**
 * 基于显示参考的PQ EOTF，显示参考和场景参考都是参考线性光，只是一个是屏幕的光，一个是采集场景内容的光
 * PQ公式参数详解见 https://juejin.cn/post/7231369710024310821#heading-13
 */
class PQDisplayEOTF : GammaEOTF() {
    val m1 = "${prefix}M1"
    val m2 = "${prefix}M2"
    val c1 = "${prefix}C1"
    val c2 = "${prefix}C2"
    val c3 = "${prefix}C3"

    override val code: String
        get() = """
        #define $m1  0.1593017578125
        #define $m2  78.84375
        #define $c1  0.8359375
        #define $c2  18.8515625
        #define $c3  18.6875
        
        float $methodGamma(float color)
        {
             color = clamp(color, 0.0, 1.0);
             float p = pow(color, 1.0 / $m2);
             float num = max(p - $c1, 0.0);
             float den = $c2 - $c3 * p;
             return  pow(num / den, 1.0 / $m1);
        }
        
        vec3 $methodGamma(vec3 color)
        {
            return vec3(
            $methodGamma(color.x),
            $methodGamma(color.y),
            $methodGamma(color.z));
        }
        """.trimIndent()
}