package com.norman.android.hdrsample.transform.shader.tonemap

import com.norman.android.hdrsample.transform.shader.MetaDataParams
import com.norman.android.hdrsample.transform.shader.ReScale.methodScaleReferenceWhiteToOne

/**
 * 该实现是BT2446中介绍的a方法
 * 个人理解，流程大约明白了，具体里面的参数为什么这么设还没不清楚
 * 1. RGB转换YCBCR
 * 2. Y亮度转换为感知线性空间
 * 3. 在感知域中对Y应用拐点函数
 * 4. 转换回伽玛域
 * 5. YCBCR转换回RGB
 * https://www.itu.int/pub/R-REP-BT.2446
 * 参考代码： https://github.com/gopro/gopro-lib-node.gl/blob/main/libnodegl/src/glsl/hdr.glsl
 *
 * //
 */
class ToneMapBT2446A : ToneMap() {
    override val code: String
        get() = """
            const float a = 0.2627002120112671;
            const float b = 0.6779980715188708;
            const float c = 0.05930171646986196;
            const float d = 2.0 * (1.0 - c);
            const float e = 2.0 * (1.0 - a);

            vec3 RGB_to_YCbCr(vec3 RGB) {
                float R = RGB.r;
                float G = RGB.g;
                float B = RGB.b;

                 float Y  = dot(RGB, vec3(a, b, c));
                 float Cb = (B - Y) / d;
                 float Cr = (R - Y) / e;

                return vec3(Y, Cb, Cr);
            }

            vec3 YCbCr_to_RGB(vec3 YCbCr) {
                float Y  = YCbCr.x;
                float Cb = YCbCr.y;
                float Cr = YCbCr.z;

                 float R = Y + e * Cr;
                 float G = Y - (a * e / b) * Cr - (c * d / b) * Cb;
                 float B = Y + d * Cb;

                return vec3(R, G, B);
            }

            float f(float Y) {
                Y = pow(Y, 1.0 / 2.4);

                 float pHDR = 1.0 + 32.0 * pow(${MetaDataParams.HDR_PEAK_LUMINANCE}/ ${MetaDataParams.PQ_MAX_LUMINANCE} / 10000.0, 1.0 / 2.4);
                 float pSDR = 1.0 + 32.0 * pow(${MetaDataParams.HDR_REFERENCE_WHITE} / ${MetaDataParams.PQ_MAX_LUMINANCE}, 1.0 / 2.4);

                 float Yp = log(1.0 + (pHDR - 1.0) * Y) / log(pHDR);

                float Yc;
                if      (Yp <= 0.7399)  Yc = Yp * 1.0770;
                else if (Yp <  0.9909)  Yc = Yp * (-1.1510 * Yp + 2.7811) - 0.6302;
                else                    Yc = Yp * 0.5000 + 0.5000;

                 float Ysdr = (pow(pSDR, Yc) - 1.0) / (pSDR - 1.0);

                Y = pow(Ysdr, 2.4);

                return Y;
            }

            vec3 tone_mapping(vec3 YCbCr) {
                 float W = ${MetaDataParams.HDR_PEAK_LUMINANCE} / ${MetaDataParams.HDR_REFERENCE_WHITE};
                YCbCr /= W;

                float Y  = YCbCr.r;
                float Cb = YCbCr.g;
                float Cr = YCbCr.b;

                 float Ysdr = f(Y);

                 float Yr = Ysdr / (1.1 * Y);
                Cb *= Yr;
                Cr *= Yr;
                Y = Ysdr - max(0.1 * Cr, 0.0);

                return vec3(Y, Cb, Cr);
            }


            /* BT.2446-1-2021 method A */
            vec3 $methodToneMap(vec3 color)
            {   
                  color = $methodScaleReferenceWhiteToOne(color);
                  color = RGB_to_YCbCr(color.rgb);
                  color = tone_mapping(color.rgb);
                  color = YCbCr_to_RGB(color.rgb);

                  return color;
            }
            """.trimIndent()
}