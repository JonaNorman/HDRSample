package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode
import com.norman.android.hdrsample.transform.shader.MetaDataParams.HLG_MAX_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.MAX_CONTENT_LUMINANCE

//参考代码:https://github.com/FFmpeg/FFmpeg/blob/master/libavfilter/vf_tonemap.c
// https://blog.csdn.net/a360940265a/article/details/124671992
// Filmic curve by John Hable, Also known as the "Uncharted 2 curve".
// http://filmicworlds.com/blog/filmic-tonemapping-operators/
object ToneMappingHable : ToneMap() {
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
                float sig_orig = max(max(rgb.r, rgb.g), rgb.b);
                float peak = $MAX_CONTENT_LUMINANCE/ $HLG_MAX_LUMINANCE;
                float sig = hable(sig_orig) / hable(peak);
                return  rgb  * sig / sig_orig;
            }
        """.trimIndent()
}