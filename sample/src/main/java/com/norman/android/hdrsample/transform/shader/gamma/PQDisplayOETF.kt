package com.norman.android.hdrsample.transform.shader.gamma

/**
 * 基于显示参考的PQ OETF
 * PQ公式参数详解见 https://juejin.cn/post/7231369710024310821#heading-13
 */
class PQDisplayOETF : GammaOETF() {

    val m1 = "${prefix}M1"
    val m2 = "${prefix}M2"
    val c1 = "${prefix}C1"
    val c2 = "${prefix}C2"
    val c3 = "${prefix}C3"

    private  val methodInverseEOTF = "${prefix}INVERSE_EOTF"

    override val code: String
        get() = """
        #define $m1  0.1593017578125
        #define $m2  78.84375
        #define $c1  0.8359375
        #define $c2  18.8515625
        #define $c3  18.6875
        
         // EOTF的逆函数
        float $methodInverseEOTF(float x)
        {
              float Ym = pow(x, $m1);
              return pow(($c1 + $c2 * Ym) / (1.0 + $c3 * Ym), $m2);
        }

        float $methodGamma(float x){
             return $methodInverseEOTF(x);
        }
        
        vec3 $methodGamma(vec3 color){
         return vec3(
            $methodGamma(color.x),
            $methodGamma(color.y),
            $methodGamma(color.z));
        }
        
        """.trimIndent()
}