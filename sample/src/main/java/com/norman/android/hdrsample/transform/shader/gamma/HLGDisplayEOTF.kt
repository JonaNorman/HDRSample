package com.norman.android.hdrsample.transform.shader.gamma

import com.norman.android.hdrsample.transform.shader.MetaDataParams.MAX_DISPLAY_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.MIN_DISPLAY_LUMINANCE


// HLG公式参数详解见 https://juejin.cn/post/7231369710024310821#heading-8
class HLGDisplayEOTF : GammaEOTF() {
    val methodInverseOETF = "${prefix}INVERSE_OETF"
    val methodHLGGamma = "${prefix}GAMMA"
    val methodOOTF = "${prefix}OOTF"
    val methodBlackLift = "${prefix}BLACK_LIFT"


    val a = "${prefix}A"
    val b = "${prefix}B"
    val c = "${prefix}C"
    val minNits= "${prefix}MIN_NITS"
    val maxNits= "${prefix}MAX_NITS"
    val bt2020LumaCoefficient= "${prefix}BT2020_LUMA_COEFFICIENT"

    override val code: String
        get() = """
        #define  $maxNits 1000.0  //HLG最大亮度
        #define  $minNits 500.0// 防止HLG亮度过低时黑暗场景过于明亮，参考至Android的computeHlgGamma做法
        #define  $a  0.17883277// ABC三个参数是为了平滑连接HLG的两端曲线
        #define  $b  0.28466892
        #define  $c  0.55991073
        #define  $bt2020LumaCoefficient vec3(0.262700, 0.677998, 0.059302)
        
      
        vec3 $methodInverseOETF(vec3 x)
        {
            return mix(x * x / 3.0,
            (exp((x - $c) / $a)+$b) / 12.0,
            step(0.5, x));
        }

        // HLG的系统伽马，根据设备亮度调整，1000亮度时候系统伽马是1.2
        float $methodHLGGamma(float lw){
            lw = max(lw,$minNits);
            return 1.2+0.42*log(lw/$maxNits)/log(10.0);
        }

        vec3 $methodOOTF(vec3 x)
        {
            float Y = dot($bt2020LumaCoefficient, x);
            float gamma = $methodHLGGamma($MAX_DISPLAY_LUMINANCE);
            return x * pow(Y,gamma-1.0);
        }

        vec3 $methodBlackLift(vec3 x){//调整黑电平，range表示亮度的范围，x表示最小，y表示最大
            float gamma = $methodHLGGamma($MAX_DISPLAY_LUMINANCE);
            float beta = sqrt(3.0 * 
            pow($MIN_DISPLAY_LUMINANCE/$MAX_DISPLAY_LUMINANCE, 
            1.0/gamma));
            return max(vec3(0.0), (1.0-beta)*x + beta);
        }

        vec3 $methodGamma(vec3 x)
        {
            return $methodOOTF($methodInverseOETF($methodBlackLift(x)));
        }
        """.trimIndent()

}
