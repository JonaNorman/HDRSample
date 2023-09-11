package com.norman.android.hdrsample.transform.shader.gamma

/**
 * 基于场景参考的PQ EOTF，显示参考和场景参考都是参考线性光，只是一个是屏幕的光，一个是采集场景内容的光
 * PQ公式参数详解见 https://juejin.cn/post/7231369710024310821#heading-13
 */
class PQSceneEOTF : GammaEOTF() {

    private val methodInverseOOTF = "${prefix}INVERSE_OOTF"

    private val methodDisplayEOTF = "${prefix}DISPLAY_EOTF"


    val m1 = "${prefix}M1"
    val m2 = "${prefix}M2"
    val c1 = "${prefix}C1"
    val c2 = "${prefix}C2"
    val c3 = "${prefix}C3"
    val bt1886InverseEOTF= "${prefix}bt1886_inverse_eotf"
    val bt709InverseOETF= "${prefix}bt709_inverse_oetf"
    val bt709Beta = "${prefix}BT709_BETA"
    val bt709Alpha = "${prefix}BT709_ALPHA"
    val ootfScale = "${prefix}OOTF_SCALE"


    override val code: String
        get() = """
        #define $m1  0.1593017578125
        #define $m2  78.84375
        #define $c1  0.8359375
        #define $c2  18.8515625
        #define $c3  18.6875
        #define $bt709Beta  0.018053968510807
        #define $bt709Alpha 1.09929682680944
        #define $ootfScale  59.49080238715383
        
        float $bt1886InverseEOTF(float x)
        {
        	return mix(0.0,pow(x, 1.0 / 2.4),step(0.0,x));
        }
        
        float $bt709InverseOETF(float x)
        {
        	x = max(x, 0.0);
        	if (x < 4.5 * $bt709Beta)
        		x = x / 4.5;
        	else
        		x = pow((x + ($bt709Alpha - 1.0)) / $bt709Alpha, 1.0 / 0.45);
        	return x;
        }
        
        float $methodInverseOOTF(float x){
             return $bt709InverseOETF($bt1886InverseEOTF(x * 100.0)) / $ootfScale;
        }
        
        float $methodDisplayEOTF(float color)
        {
            if(color<=0.0) return 0.0;
            float p = pow(color, 1.0 / $m2);
            float num = max(p - $c1, 0.0);
            float den = $c2 - $c3 * p;
            return pow(num / den, 1.0 / $m1);
        }
        
        float $methodGamma(float color)
        {
            return $methodInverseOOTF($methodDisplayEOTF(color));
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