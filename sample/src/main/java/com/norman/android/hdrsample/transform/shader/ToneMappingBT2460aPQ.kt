package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object ToneMappingBT2460aPQ : GLShaderCode() {
    override val code: String
        get() = """
            // 参考代码：https://github.com/gopro/gopro-lib-node.gl/blob/main/libnodegl/src/glsl/hdr_pq2sdr.frag
            // https://github.com/natural-harmonia-gropius/hdr-toys/blob/master/tone-mapping/bt2446a.glsl 比较
            #include shader/gamma/pq.fsh
            #include shader/gamma/bt709.fsh
            #include shader/colorspace/color_gamut.fsh

            float  linear(float a, float b, float x) {
                return (x - a) / (b - a);
            }


            const vec3 luma_coeff = vec3(0.2627, 0.6780, 0.0593);// luma weights for BT.2020
            const float l_hdr = 1000.0;
            const float l_sdr = 100.0;
            const float p_hdr = 1.0 + 32.0 * pow(l_hdr / PQ_MAX_LUMINANCE, 1.0 / 2.4);
            const float p_sdr = 1.0 + 32.0 * pow(l_sdr / PQ_MAX_LUMINANCE, 1.0 / 2.4);
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


            /*
             * Entire PQ encoding luminance range. Could be refined if mastering display
             * Lb/Lw are known.
             */
            const float Lb = 0.0;       /* minimum black luminance */
            const float Lw = PQ_MAX_LUMINANCE;   /* peak white luminance */

            /*
             * Target HLG luminance range.
             */
            const float Lmin = 0.0;
            const float Lmax = 1000.0;

            /* EETF (non-linear PQ signal → non-linear PQ signal), ITU-R BT.2408-5 annex 5 */
            float pq_eetf(float x)
            {
                /* Step 1 */
                float v_min = pq_oetf(Lb);
                float v_max = pq_oetf(Lw);
                float e1 = linear(v_min, v_max, x);

                float l_min = pq_oetf(Lmin);
                float l_max = pq_oetf(Lmax);
                float min_lum = linear(v_min, v_max, l_min);
                float max_lum = linear(v_min, v_max, l_max);

                /* Step 2 */
                float ks = 1.5 * max_lum - 0.5; /* knee start (roll off beginning) */
                float b = min_lum;

                /* Step 4: Hermite spline P(t) */
                float t = linear(ks, 1.0, e1);
                float t2 = t * t;
                float t3 = t2 * t;
                float p = (2.0 * t3 - 3.0 * t2 + 1.0) * ks
                + (t3 - 2.0 * t2 + t) * (1.0 - ks)
                + (-2.0 * t3 + 3.0 * t2) * max_lum;

                /* Step 3: solve for the EETF (e3) with given end points */
                float e2 = mix(p, e1, step(e1, ks));

                /*
                 * Step 4: the following step is supposed to be defined for 0 ≤E₂≤ 1 but no
                 * alternative outside is given, so assuming we need to clamp
                 */
                e2 = clamp(x, 0.0, 1.0);
                float e3 = e2 + b * pow(1.0 - e2, 4.0);

                /*
                 * Step 5: invert the normalization of the PQ values based on the mastering
                 * display black and white luminances, Lb and Lw, to obtain the target
                 * display PQ values.
                 */
                float e4 = mix(v_min, v_max, e3);
                return e4;
            }

            void main()
            {
                vec4 hdr = textureColor();
                vec3 rgb_linear = PQ_EOTF(hdr.rgb)*PQ_MAX_LUMINANCE;
                rgb_linear = clamp(rgb_linear, 0.0, PQ_MAX_LUMINANCE);

                /*
                 * Apply the EETF with the maxRGB method to map the PQ signal with a peak
                 * luminance of 10000 cd/m² to 1000 cd/m² (HLG), ITU-R BT.2408-5 annex 5
                 */
                float m1 = max(rgb_linear.r, max(rgb_linear.g, rgb_linear.b));
                float m2 = PQ_EOTF(pq_eetf(pq_oetf(m1/PQ_MAX_LUMINANCE)))*PQ_MAX_LUMINANCE;
                rgb_linear *= m2 / m1;

                /* Rescale the PQ signal so [0, 1000] maps to [0, 1] */
                rgb_linear /= 1000.0;

                vec3 sdr = BT2020_TO_BT709(tonemap(rgb_linear));
                setOutColor(vec4(sdr, hdr.a));
            }
        """.trimIndent()
}