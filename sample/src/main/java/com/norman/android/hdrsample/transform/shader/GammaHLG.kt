package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object GammaHLG : GLShaderCode() {
    // HLG公式参数详解见 https://juejin.cn/post/7231369710024310821#heading-8
    override val code: String
        get() = """
        #define  BT2020_LUMA_COEFF  vec3(0.2627, 0.6780, 0.0593)// BT.2020RGB转亮度Y的计算系数
        #define  HLG_A  0.17883277// ABC三个参数是为了平滑连接HLG的两端曲线
        #define  HLG_B  0.28466892
        #define  HLG_C  0.55991073
        #define  HLG_MAX_LUMINANCE  1000.0// HLG的最大亮度
        #define  HLG_REFERENCE_WHITE 203.0// HLG参考白


        vec3 HLG_OETF(vec3 x)
        {
            return mix(sqrt(3.0 * x),
            HLG_A * log(12.0 * x - HLG_B) + HLG_C,
            step(1.0 / 12.0, x));
        }

        // OETF的逆函数
        vec3 HLG_OETF_1(vec3 x)
        {
            return mix(x * x / 3.0,
            (HLG_B+exp((x - HLG_C) / HLG_A)) / 12.0,
            step(0.5, x));
        }

        // HLG的系统伽马，根据设备亮度调整，1000亮度时候系统伽马是1.2
        float HLG_GAMMA(float lw){
            return 1.2+0.42*log(lw/HLG_MAX_LUMINANCE)/log(10.0);
        }

        vec3 HLG_OOTF(vec3 x, float lw)
        {
            return x * pow(dot(BT2020_LUMA_COEFF, x),HLG_GAMMA(lw)-1.0);
        }

        vec3 HLG_BLACK_LIFT(vec3 x, vec2 range){//调整黑电平，range表示亮度的范围，x表示最小，y表示最大
            float b = sqrt(3.0 * pow(range.x/range.y, 1.0/HLG_GAMMA(range.y)));
            return max(vec3(0.0), (1.0-b)*x + b);
        }


        vec3 HLG_EOTF(vec3 x, vec2 range)
        {
            return HLG_OOTF(HLG_OETF_1(HLG_BLACK_LIFT(x, range)), range.y);
        }
        """.trimIndent()

}
