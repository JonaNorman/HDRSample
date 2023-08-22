package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.transform.shader.MetaDataParams.COLOR_SPACE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.COLOR_SPACE_BT2020_HLG
import com.norman.android.hdrsample.transform.shader.MetaDataParams.COLOR_SPACE_BT2020_PQ
import com.norman.android.hdrsample.transform.shader.MetaDataParams.MAX_DISPLAY_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.MAX_FRAME_AVERAGE_LUMINANCE
import com.norman.android.hdrsample.transform.shader.ColorSpaceConversion.methodBt2020ToXYZ
import com.norman.android.hdrsample.transform.shader.ColorSpaceConversion.methodXYZToBt2020
import com.norman.android.hdrsample.transform.shader.ConstantParams.HLG_MAX_LUMINANCE
import com.norman.android.hdrsample.transform.shader.ConstantParams.PQ_MAX_LUMINANCE

//参考地址：https://android.googlesource.com/platform/frameworks/native/+/refs/heads/master/libs/tonemap/tonemap.cpp

object ToneMapAndroid8 : ToneMap() {
    override val code: String
        get() = """
            
        float libtonemap_applyBaseOOTFGain(float nits) {
               return $COLOR_SPACE == $COLOR_SPACE_BT2020_HLG?pow(nits, 0.2): 1.0;
        }
       
       vec3 ScaleLuminance(vec3 xyz) {
               return $COLOR_SPACE == $COLOR_SPACE_BT2020_PQ?xyz * $PQ_MAX_LUMINANCE:xyz * $HLG_MAX_LUMINANCE;         
       }
        
       vec3 NormalizeLuminance(vec3 xyz) {
              return xyz / $MAX_DISPLAY_LUMINANCE;
       }

        // Here we're mapping from HDR to SDR content, so interpolate using a
                        // Hermitian polynomial onto the smaller luminance range.
        float toneMapTargetNits(vec3 xyz) {
            float maxInLumi = $MAX_FRAME_AVERAGE_LUMINANCE;
            float maxOutLumi = $MAX_DISPLAY_LUMINANCE;
            xyz = xyz * libtonemap_applyBaseOOTFGain(xyz.y);
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
            vec3 xyz = $methodBt2020ToXYZ(rgb)
            vec3 absoluteXYZ = ScaleLuminance(xyz);
            float gain = lookupTonemapGain(absoluteXYZ);
            xyz = NormalizeLuminance(absoluteXYZ * gain);
            return $methodXYZToBt2020(xyz);
        }
        """.trimIndent()
}