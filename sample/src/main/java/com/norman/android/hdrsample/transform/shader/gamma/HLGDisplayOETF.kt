package com.norman.android.hdrsample.transform.shader.gamma

import com.norman.android.hdrsample.transform.shader.MetaDataParams.MAX_DISPLAY_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.MIN_DISPLAY_LUMINANCE


// HLG公式参数详解见 https://juejin.cn/post/7231369710024310821#heading-8
class HLGDisplayOETF : GammaEOTF() {
    val methodSceneOETF = "${prefix}SCENE_OETF"
    val methodHLGGamma = "${prefix}GAMMA"
    val methodInverseOOTF = "${prefix}INVERSE_OOTF"
    val methodInverseBlackLift = "${prefix}INVERSE_BLACK_LIFT"

    val a = "${prefix}A"
    val b = "${prefix}B"
    val c = "${prefix}C"
    val minNits = "${prefix}MIN_NITS"
    val maxNits = "${prefix}MAX_NITS"

    override val code: String
        get() = """
        #define  $maxNits 1000.0  //HLG最大亮度
        #define  $minNits 500.0// 防止HLG亮度过低时黑暗场景过于明亮，参考至Android的computeHlgGamma做法
        #define  $a  0.17883277// ABC三个参数是为了平滑连接HLG的两端曲线
        #define  $b  0.28466892
        #define  $c  0.55991073
        
       
        vec3 $methodSceneOETF(vec3 x)
        {
             return mix(sqrt(3.0 * x),
            $a * log(12.0 * x - $b) + $c,
            step(1.0 / 12.0, x));
        }

        // HLG的系统伽马，根据设备亮度调整，1000亮度时候系统伽马是1.2
        float $methodHLGGamma(float lw){
            lw = max(lw,$minNits);
            return 1.2+0.42*log(lw/$maxNits)/log(10.0);
        }

        vec3 $methodInverseOOTF(vec3 x)
        {
            float gamma = $methodHLGGamma($MAX_DISPLAY_LUMINANCE);
            float Y = dot(vec3(0.262700, 0.677998, 0.059302), x);
            return x*pow(Y,(1.0-gamma)/gamma);
        }

        vec3 $methodInverseBlackLift(vec3 x){//
            float beta = sqrt(3.0 * 
            pow($MIN_DISPLAY_LUMINANCE/$MAX_DISPLAY_LUMINANCE, 
            1.0/$methodHLGGamma($MAX_DISPLAY_LUMINANCE)));
            return max(vec3(0.0), (x-beta)/(1.0-beta));
        }

        vec3 $methodGamma(vec3 x)
        {
            return $methodInverseBlackLift($methodSceneOETF($methodInverseOOTF(x)));
        }
        """.trimIndent()

}
