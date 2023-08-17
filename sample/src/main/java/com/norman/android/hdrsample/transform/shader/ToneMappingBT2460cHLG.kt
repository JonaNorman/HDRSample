package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object ToneMappingBT2460cHLG : GLShaderCode() {
    override val code: String
        get() = """
            //https://github.com/toru-ver4/sample_code/blob/develop/ty_lib/bt2446_method_c.py
            // BT2446-C的曲线可视化 https://www.desmos.com/calculator/rv08vuzqjk?lang=zh-CN
            // BT2446-C的曲线可视化https://www.desmos.com/calculator/1dwlw3ultd?lang=ru
            //https://trev16.hatenablog.com/entry/2020/08/01/131907

            //https://github.com/natural-harmonia-gropius/hdr-toys


            #include shader/gamma/pq.fsh
            #include shader/gamma/bt709.fsh
            #include shader/colorspace/color_gamut.fsh

            const float ip = 0.58535;// linear length
            const float k1 = 0.83802;// linear strength
            const float k3 = 0.74204;// shoulder strength

            float f(float Y, float k1, float k3, float ip) {
                ip /= k1;
                float k2 = (k1 * ip) * (1.0 - k3);
                float k4 = (k1 * ip) - (k2 * log(1.0 - k3));
                return Y < ip ?
                Y * k1 :
                log((Y / ip) - k3) * k2 + k4;
            }

            float curve(float x) {
                const float over_white = 1019.0 / 940.0;// 109% range (super-whites)
                return f(x, k1, k3, ip) / over_white;
            }

            vec3 tonemap(vec3 rgb) {
                vec3 xyz = BT2020_TO_XYZ(rgb);
                vec3 xyY = XYZ_TO_xyY(xyz);
                float Y   = curve(xyY.z);
                xyz = xyY_to_XYZ(vec3(xyY.x, xyY.y, Y));
                return XYZ_TO_BT2020(xyz);
            }

            void main()
            {
                vec4 hdr = textureColor();
                vec3 sdr = BT2020_TO_BT709(tonemap(HLG_EOTF(hdr.rgb)));
                setOutColor(vec4(sdr, hdr.a));
            }
        """.trimIndent()
}