package com.norman.android.hdrsample.transform.shader.tonemap

import com.norman.android.hdrsample.transform.shader.MetaDataParams.HDR_REFERENCE_WHITE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.HDR_PEAK_LUMINANCE
import com.norman.android.hdrsample.transform.shader.ReScale.methodScaleReferenceWhiteToOne

/**
 *   https://blog.csdn.net/a360940265a/article/details/124671992
 *  Filmic curve by John Hable, Also known as the "Uncharted 2 curve".
 *  http://filmicworlds.com/blog/filmic-tonemapping-operators/
 *  参考代码:https://github.com/FFmpeg/FFmpeg/blob/master/libavfilter/vf_tonemap.c
 */
class ToneMappingHable : ToneMap() {
    override val code: String
        get() = """
            const float A = 0.15;   // Shoulder Strength
            const float B = 0.50;   // Linear Strength
            const float C = 0.10;   // Linear Angle
            const float D = 0.20;   // Toe Strength
            const float E = 0.02;   // Toe Numerator
            const float F = 0.30;   // Toe Denominator

            float hable(float x)
            {
                 return ((x * (A * x + C * B) + D * E) / (x * (A * x + B) + D * F)) - E / F;
            }

            vec3 $methodToneMap(vec3 rgb) {
                rgb = $methodScaleReferenceWhiteToOne(rgb);
                float sig_orig = max(max(rgb.r, rgb.g), rgb.b);
                float peak = $HDR_PEAK_LUMINANCE/$HDR_REFERENCE_WHITE;
                float sig = hable(sig_orig) / hable(peak);
                return  rgb  * sig / sig_orig;
            }
        """.trimIndent()
}