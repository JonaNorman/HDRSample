package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object ToneMappingBT2460aHLG : GLShaderCode() {
    override val code: String
        get() = """
            // 参考代码： https://github.com/gopro/gopro-lib-node.gl/blob/main/libnodegl/src/glsl/hdr_hlg2sdr.frag
            #include shader/gamma/hlg.fsh
            #include shader/gamma/bt709.fsh
            #include shader/colorspace/color_gamut.fsh
            const vec3 luma_coeff = vec3(0.2627, 0.6780, 0.0593); // luma weights for BT.2020
            const float l_hdr = 1000.0;
            const float l_sdr = 100.0;
            const float p_hdr = 1.0 + 32.0 * pow(l_hdr / 10000.0, 1.0 / 2.4);
            const float p_sdr = 1.0 + 32.0 * pow(l_sdr / 10000.0, 1.0 / 2.4);
            const float gcr = luma_coeff.r / luma_coeff.g;
            const float gcb = luma_coeff.b / luma_coeff.g;

            /* BT.2446-1-2021 method A */
            vec3 tonemap(vec3 x)
            {
                vec3 xp = pow(x, vec3(1.0 / 2.4));
                float y_hdr = dot(luma_coeff, xp);

                /* Step 1: convert signal to perceptually linear space */
                float yp = log(1.0 + (p_hdr - 1.0) * y_hdr) / log(p_hdr);

                /* Step 2: apply knee function in perceptual domain */
                float yc = mix(
                1.077 * yp,
                mix((-1.1510 * yp + 2.7811) * yp - 0.6302, 0.5 * yp + 0.5, yp > 0.9909 ? 1.0:0.0),
                yp > 0.7399? 1.0:0.0);

                /* Step 3: convert back to gamma domain */
                float y_sdr = (pow(p_sdr, yc) - 1.0) / (p_sdr - 1.0);

                /* Colour correction */
                float scale = y_sdr / (1.1 * y_hdr);
                float cb_tmo = scale * (xp.b - y_hdr);
                float cr_tmo = scale * (xp.r - y_hdr);
                float y_tmo = y_sdr - max(0.1 * cr_tmo, 0.0);

                /* Convert from Y'Cb'Cr' to R'G'B' (still in BT.2020) */
                float cg_tmo = -(gcr * cr_tmo + gcb * cb_tmo);
                return y_tmo + vec3(cr_tmo, cg_tmo, cb_tmo);
            }

            void main()
            {
                vec4 hdr = textureColor();
                vec3 sdr = BT2020_TO_BT709(tonemap(HLG_EOTF(hdr.rgb)));
                setOutColor(vec4(sdr, hdr.a));
            }
        """.trimIndent()
}