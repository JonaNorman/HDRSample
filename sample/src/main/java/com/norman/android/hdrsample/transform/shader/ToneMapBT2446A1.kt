package com.norman.android.hdrsample.transform.shader

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
object ToneMapBT2446A1 : ToneMap() {
    override val code: String
        get() = """
            #define ngli_sat(x) clamp(x, 0.0, 1.0)
            #define ngli_linear(a, b, x) (((x) - (a)) / ((b) - (a)))
            
            /*
             * Entire PQ encoding luminance range. Could be refined if mastering display
             * Lb/Lw are known.
             */
            const float Lb = 0.0;       /* minimum black luminance */
            const float Lw = 10000.0;   /* peak white luminance */

            /*
             * Target HLG luminance range.
             */
            const float Lmin = 0.0;
            const float Lmax = 1000.0;
            
            float pq_eotf(float x)
            {
                return ${GammaPQ.methodEOTF}(x)*10000.0;
            }

         
            float pq_oetf(float x)
            {
              
                return ${GammaPQ.methodOETF}(x/10000.0);
            }

            /* EETF (non-linear PQ signal → non-linear PQ signal), ITU-R BT.2408-5 annex 5 */
            float pq_eetf(float x)
            {
                /* Step 1 */
                float v_min = pq_oetf(Lb);
                float v_max = pq_oetf(Lw);
                float e1 = ngli_linear(v_min, v_max, x);

                float l_min = pq_oetf(Lmin);
                float l_max = pq_oetf(Lmax);
                float min_lum = ngli_linear(v_min, v_max, l_min);
                float max_lum = ngli_linear(v_min, v_max, l_max);

                /* Step 2 */
                float ks = 1.5 * max_lum - 0.5; /* knee start (roll off beginning) */
                float b = min_lum;

                /* Step 4: Hermite spline P(t) */
                float t = ngli_linear(ks, 1.0, e1);
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
                e2 = ngli_sat(e2);
                float e3 = e2 + b * pow(1.0 - e2, 4.0);

                /*
                 * Step 5: invert the normalization of the PQ values based on the mastering
                 * display black and white luminances, Lb and Lw, to obtain the target
                 * display PQ values.
                 */
                float e4 = mix(v_min, v_max, e3);
                return e4;
            }
            
            
            vec3 pq1000(vec3 color){

                /*
                 * Apply the EETF with the maxRGB method to map the PQ signal with a peak
                 * luminance of 10000 cd/m² to 1000 cd/m² (HLG), ITU-R BT.2408-5 annex 5
                 */
                color = color *10000.0;
                float m1 = max(color.r, max(color.g, color.b));
                float m2 = pq_eotf(pq_eetf(pq_oetf(m1)));
                color *= m2 / m1;

                /* Rescale the PQ signal so [0, 1000] maps to [0, 1] */
                color /= 1000.0;
                return color;
            }
         
            const vec3 luma_coeff = vec3(0.262700, 0.677998, 0.059302); // luma weights for BT.2020
            const float gcr = luma_coeff.r / luma_coeff.g;
            const float gcb = luma_coeff.b / luma_coeff.g;

            /* BT.2446-1-2021 method A */
            vec3 $methodToneMap(vec3 color)
            {   
                color = pq1000(color);
                float p_hdr = 1.0 + 32.0 * pow(${MetaDataParams.HDR_PEAK_LUMINANCE}/ ${MetaDataParams.PQ_MAX_LUMINANCE}, 1.0 / 2.4);
                float p_sdr = 1.0 + 32.0 * pow(${MetaDataParams.HDR_REFERENCE_WHITE} / ${MetaDataParams.PQ_MAX_LUMINANCE}, 1.0 / 2.4);
                vec3 xp = pow(color, vec3(1.0 / 2.4));
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
                color = y_tmo + vec3(cr_tmo, cg_tmo, cb_tmo);
                return color;
            }
            """.trimIndent()
}