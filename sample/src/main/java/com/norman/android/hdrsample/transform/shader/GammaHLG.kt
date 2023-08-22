package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode
import com.norman.android.hdrsample.transform.shader.ColorSpaceConversion.methodBt2020ToXYZ
import com.norman.android.hdrsample.transform.shader.MetaDataParams.HDR_REFERENCE_WHITE

// HLG公式参数详解见 https://juejin.cn/post/7231369710024310821#heading-8
object GammaHLG : GLShaderCode() {
    const val methodHLGOETF  = "HLG_OETF"
    const val methodHLGOETFInv  = "HLG_OETF_1"
    const val methodHLGGamma  = "HLG_GAMMA"
    const val methodHLGOOTF  = "HLG_OOTF"
    const val methodHLGBlackLift  = "HLG_BLACK_LIFT"
    const val methodHLGEOTF  = "HLG_EOTF"


    init {
        includeList.add(ColorSpaceConversion)
        includeList.add(MetaDataParams)
    }
    override val code: String
        get() = """
        #define  HLG_A  0.17883277// ABC三个参数是为了平滑连接HLG的两端曲线
        #define  HLG_B  0.28466892
        #define  HLG_C  0.55991073
        #define  HLG_MIN_BRIGHTNESS_NITS 500.0// 防止HLG亮度过低时黑暗场景过于明亮，参考至Android的computeHlgGamma做法


        vec3 $methodHLGOETF(vec3 x)
        {
            return mix(sqrt(3.0 * x),
            HLG_A * log(12.0 * x - HLG_B) + HLG_C,
            step(1.0 / 12.0, x));
        }

        // OETF的逆函数
        vec3 $methodHLGOETFInv(vec3 x)
        {
            return mix(x * x / 3.0,
            (HLG_B+exp((x - HLG_C) / HLG_A)) / 12.0,
            step(0.5, x));
        }

        // HLG的系统伽马，根据设备亮度调整，1000亮度时候系统伽马是1.2，
        float $methodHLGGamma(float lw){
            lw = max(lw,HLG_MIN_BRIGHTNESS_NITS);
            return 1.2+0.42*log(lw/$HDR_REFERENCE_WHITE)/log(10.0);
        }

        vec3 $methodHLGOOTF(vec3 x, float lw)
        {
            return x * pow($methodBt2020ToXYZ(x).y,$methodHLGGamma(lw)-1.0);
        }

        vec3 $methodHLGBlackLift(vec3 x, vec2 range){//调整黑电平，range表示亮度的范围，x表示最小，y表示最大
            float b = sqrt(3.0 * pow(range.x/range.y, 1.0/$methodHLGGamma(range.y)));
            return max(vec3(0.0), (1.0-b)*x + b);
        }


        vec3 $methodHLGEOTF(vec3 x, vec2 range)
        {
            return $methodHLGOOTF($methodHLGOETFInv($methodHLGBlackLift(x, range)), range.y);
        }
        """.trimIndent()

}
