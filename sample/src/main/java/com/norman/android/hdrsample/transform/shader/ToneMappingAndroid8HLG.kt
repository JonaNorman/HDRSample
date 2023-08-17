package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object ToneMappingAndroid8HLG : GLShaderCode() {
    override val code: String
        get() = """
      //参考地址：https://android.googlesource.com/platform/frameworks/native/+/refs/heads/master/libs/tonemap/tonemap.cpp
        #include shader/gamma/hlg.fsh
        #include shader/gamma/bt709.fsh
        #include shader/colorspace/color_gamut.fsh

        uniform vec2 displayLuminanceRange;// 屏幕最大亮度
        uniform float maxFrameAverageLuminance;// 最大平均亮度


        float toneMapTargetNits(vec3 xyz) {
            float maxInLumi = maxFrameAverageLuminance;
            float maxOutLumi = displayLuminanceRange.y;
            xyz = xyz * pow(xyz.y, 0.2);
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

        float lookupTonemapGain(vec3 linearRGB, vec3 xyz) {
            if (xyz.y <= 0.0) {
                return 1.0;
            }
            return toneMapTargetNits(xyz) / xyz.y;
        }

        void main()
        {
            vec4 rgba  = textureColor();
            vec3 linearRGB = HLG_EOTF(rgba.rgb,displayLuminanceRange);
            vec3 xyz = BT2020_TO_XYZ(linearRGB);
            vec3 absoluteRGB = linearRGB *HLG_MAX_LUMINANCE;
            vec3 absoluteXYZ = xyz *HLG_MAX_LUMINANCE;
            float gain = lookupTonemapGain(absoluteRGB,absoluteXYZ);
            xyz = absoluteXYZ * gain/displayLuminanceRange.y;
            vec3 finalColor = BT709_OETF(XYZ_TO_BT709(xyz));
            setOutColor(vec4(finalColor, rgba.a));
        }
        """.trimIndent()
}