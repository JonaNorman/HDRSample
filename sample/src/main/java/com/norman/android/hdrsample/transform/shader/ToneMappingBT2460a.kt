package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object ToneMappingBT2460a : GLShaderCode() {
    // ITU-R BT.2446 Conversion Method A
    // https://www.itu.int/pub/R-REP-BT.2446
    override val code: String
        get() = """
            #define L_hdr 1000.0
            #define L_sdr 203.0
            const float a = 0.2627002120112671;
            const float b = 0.6779980715188708;
            const float c = 0.05930171646986196;
            const float d = 2.0 * (1.0 - c);
            const float e = 2.0 * (1.0 - a);

            vec3 RGB_to_YCbCr(vec3 RGB) {
                float R = RGB.r;
                float G = RGB.g;
                float B = RGB.b;

                const float Y  = dot(RGB, vec3(a, b, c));
                const float Cb = (B - Y) / d;
                const float Cr = (R - Y) / e;

                return vec3(Y, Cb, Cr);
            }

            vec3 YCbCr_to_RGB(vec3 YCbCr) {
                float Y  = YCbCr.x;
                float Cb = YCbCr.y;
                float Cr = YCbCr.z;

                const float R = Y + e * Cr;
                const float G = Y - (a * e / b) * Cr - (c * d / b) * Cb;
                const float B = Y + d * Cb;

                return vec3(R, G, B);
            }

            float f(float Y) {
                Y = pow(Y, 1.0 / 2.4);

                const float pHDR = 1.0 + 32.0 * pow(L_hdr / 10000.0, 1.0 / 2.4);
                const float pSDR = 1.0 + 32.0 * pow(L_sdr / 10000.0, 1.0 / 2.4);

                const float Yp = log(1.0 + (pHDR - 1.0) * Y) / log(pHDR);

                float Yc;
                if      (Yp <= 0.7399)  Yc = Yp * 1.0770;
                else if (Yp <  0.9909)  Yc = Yp * (-1.1510 * Yp + 2.7811) - 0.6302;
                else                    Yc = Yp * 0.5000 + 0.5000;

                const float Ysdr = (pow(pSDR, Yc) - 1.0) / (pSDR - 1.0);

                Y = pow(Ysdr, 2.4);

                return Y;
            }

            vec3 tone_mapping(vec3 YCbCr) {
                const float W = L_hdr / L_sdr;
                YCbCr /= W;

                float Y  = YCbCr.r;
                float Cb = YCbCr.g;
                float Cr = YCbCr.b;

                const float Ysdr = f(Y);

                const float Yr = Ysdr / (1.1 * Y);
                Cb *= Yr;
                Cr *= Yr;
                Y = Ysdr - max(0.1 * Cr, 0.0);

                return vec3(Y, Cb, Cr);
            }

            vec4 ${javaClass.name}(vec4 color) {
                color.rgb = RGB_to_YCbCr(color.rgb);
                color.rgb = tone_mapping(color.rgb);
                color.rgb = YCbCr_to_RGB(color.rgb);

                return color;
            }
        """.trimIndent()
}