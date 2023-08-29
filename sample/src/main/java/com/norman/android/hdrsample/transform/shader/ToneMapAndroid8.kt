package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.transform.shader.ColorConversion.methodBt2020ToXYZ
import com.norman.android.hdrsample.transform.shader.ColorConversion.methodXYZToBt2020
import com.norman.android.hdrsample.transform.shader.MetaDataParams.HDR_PEAK_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.MAX_DISPLAY_LUMINANCE

/**
 * Android8的实现，
 * 个人理解和Android13其实思路是一样，按几个点插值形成的曲线进行调整
 * (x0,y0) x0=10,y0=17,其实就是暗部这部分线性插值
 * (x1,y1) x1=y1等于屏幕最大亮度的0.75,也是线性插值
 * (x2,y2) x2在x1和输入最大亮度的中间，y2在y1和屏幕最大亮度的中间，然后用Hermitian曲线进行插值
 * Hermitian在BT2309中也有使用到
 * 参考地址：
 * https://android.googlesource.com/platform/frameworks/native/+/refs/heads/master/libs/tonemap/tonemap.cpp
 * https://www.itu.int/dms_pub/itu-r/opb/rep/R-REP-BT.2390-10-2021-PDF-E.pdf
 */
object ToneMapAndroid8 : ToneMap() {
    override val code: String
        get() = """
            
        // Here we're mapping from HDR to SDR content, so interpolate using a
                        // Hermitian polynomial onto the smaller luminance range.
        float toneMapTargetNits(vec3 xyz) {
            float maxInLumi = $HDR_PEAK_LUMINANCE;
            float maxOutLumi = $MAX_DISPLAY_LUMINANCE;
            float nits = xyz.y;
            // if the max input luminance is less than what we can
            // output then no tone mapping is needed as all color
            // values will be in range.
            if (maxInLumi <= maxOutLumi) {
                return xyz.y;
            } else {
                // three control points
                const float x0 = 10.0;
                const float y0 = 17.0;
                float x1 = maxOutLumi * 0.75;
                float y1 = x1;
                float x2 = x1 + (maxInLumi - x1) / 2.0;
                float y2 = y1 + (maxOutLumi - y1) * 0.75;
                // horizontal distances between the last three
                // control points
                float h12 = x2 - x1;
                float h23 = maxInLumi - x2;
                // tangents at the last three control points
                float m1 = (y2 - y1) / h12;
                float m3 = (maxOutLumi - y2) / h23;
                float m2 = (m1 + m3) / 2.0;
                if (nits < x0) {
                    // scale [0.0, x0] to [0.0, y0] linearly
                    float slope = y0 / x0;
                    return nits * slope;
                } else if (nits < x1) {
                    // scale [x0, x1] to [y0, y1] linearly
                    float slope = (y1 - y0) / (x1 - x0);
                    nits = y0 + (nits - x0) * slope;
                } else if (nits < x2) {
                    // scale [x1, x2] to [y1, y2] using Hermite interp
                    float t = (nits - x1) / h12;
                    nits = (y1 * (1.0 + 2.0 * t) + h12 * m1 * t) *
                    (1.0 - t) * (1.0 - t) +
                    (y2 * (3.0 - 2.0 * t) +
                    h12 * m2 * (t - 1.0)) * t * t;
                } else {
                    // scale [x2, maxInLumi] to [y2, maxOutLumi] using
                    // Hermite interp
                    float t = (nits - x2) / h23;
                    nits = (y2 * (1.0 + 2.0 * t) + h23 * m2 * t) *
                    (1.0 - t) * (1.0 - t) + (maxOutLumi *
                    (3.0 - 2.0 * t) + h23 * m3 *
                    (t - 1.0)) * t * t;
                }
            }
            return nits;
        }

        float lookupTonemapGain(vec3 xyz) {
            if (xyz.y <= 0.0) {
                return 1.0;
            }
            return toneMapTargetNits(xyz) / xyz.y;
        }
        

        vec3 $methodToneMap(vec3 rgb)
        {
            rgb = ${ReScale.methodScaleToMaster}(rgb);
            vec3 xyz = $methodBt2020ToXYZ(rgb);
            float gain = lookupTonemapGain(xyz);//XYZ用Hermitian曲线调整后的比值作为gain值
            xyz = xyz * gain;
            rgb =  $methodXYZToBt2020(xyz);
            rgb = ${ReScale.methodNormalizeDisplay}(rgb);
            return rgb;
        }
        """.trimIndent()
}