package com.norman.android.hdrsample.transform.shader.gamma

/**
 * 基于场景参考的PQ OETF，显示参考和场景参考都是参考线性光，只是一个是屏幕的光，一个是采集场景内容的光
 * PQ公式参数详解见 https://juejin.cn/post/7231369710024310821#heading-13
 */
class PQSceneOETF : GammaOETF() {

    val methodOOTF = "${prefix}OOTF"
    val methodInverseEOTF = "${prefix}INVERSE_EOTF"
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
        #define $c2 18.8515625
        #define $c3  18.6875
        
         // EOTF的逆函数
        float $methodInverseEOTF(float x)
        {
            if(x<=0.0) return 0.0;
            float Ym = pow(x, $m1);
            return pow(($c1 + $c2 * Ym) / (1.0 + $c3 * Ym), $m2);
        }

        float $methodOOTF(float x){
             float y =  mix(267.84*x, 
             1.099*pow(59.5208*x,0.45)-0.099, 
             step(0.0003024, x));
             return pow(y,2.4)/100.0;
        }
       
        float $methodGamma(float x){
             return $methodInverseEOTF($methodOOTF(x));
        }
        
        vec3 $methodGamma(vec3 color){
            return vec3(
            $methodGamma(color.x),
            $methodGamma(color.y),
            $methodGamma(color.z));
        }
        
        """.trimIndent()
}