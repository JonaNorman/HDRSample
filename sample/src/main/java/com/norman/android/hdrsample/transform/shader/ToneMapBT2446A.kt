package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.transform.shader.MetaDataParams.MAX_DISPLAY_LUMINANCE

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
object ToneMapBT2446A : ToneMap() {
    override val code: String
        get() = """
         
            const vec3 luma_coeff = vec3(0.262700, 0.677998, 0.059302); // luma weights for BT.2020
            const float gcr = luma_coeff.r / luma_coeff.g;
            const float gcb = luma_coeff.b / luma_coeff.g;

            /* BT.2446-1-2021 method A */
            vec3 $methodToneMap(vec3 color)
            {
                color = ${ReScale.methodScaleNormalize}(color);//输入需要归一化
                float p_hdr = 1.0 + 32.0 * pow(${MetaDataParams.MAX_CONTENT_LUMINANCE} / ${MetaDataParams.PQ_MAX_LUMINANCE}, 1.0 / 2.4);
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
                color = color*$MAX_DISPLAY_LUMINANCE;//输入的范围要0-MAX_DISPLAY_LUMINANCE
                return color;
            }
            """.trimIndent()
}