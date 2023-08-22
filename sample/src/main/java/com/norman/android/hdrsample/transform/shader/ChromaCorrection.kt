package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode
import com.norman.android.hdrsample.transform.shader.MetaDataParams.MAX_FRAME_AVERAGE_LUMINANCE
import com.norman.android.hdrsample.transform.shader.ColorSpaceConversion.methodBt2020ToLab
import com.norman.android.hdrsample.transform.shader.ColorSpaceConversion.methodLabToBT2020
import com.norman.android.hdrsample.transform.shader.ColorSpaceConversion.methodLabToLch
import com.norman.android.hdrsample.transform.shader.ColorSpaceConversion.methodLchToLab
import com.norman.android.hdrsample.transform.shader.ConstantParams.HDR_REFERENCE_WHITE

/**
 * 色度矫正，该方法是BT2446C方法介绍，对超过HDR参考白的高光去饱和使SDR内容的高亮部分看起来像白光
 * 步骤1: crosstalk矩阵解决后续高光去饱和计算色度的干扰问题，个人觉得是经验值，alpha从0-0.5
 * 步骤2: RGB转换成LAB(L亮度A绿色到红色分量B蓝色到黄色分量)，LAB再转换LCH(L亮度C色度H色调)
 * 步骤3: 对HDR参考白以上颜色LCH的C通道(色度)进行降色处理，规则是亮度越高色度越低
 * 步骤4: 还原成RGB颜色
 * https://www.itu.int/dms_pub/itu-r/opb/rep/R-REP-BT.2446-2019-PDF-E.pdf
 * https://github.com/natural-harmonia-gropius/hdr-toys/blob/master/utils/chroma_correction.glsl
 */
object ChromaCorrection : GLShaderCode() {

    const val paramSigma = "SIGMA"//高光去饱和的程度
    const val paramAlpha = "ALPHA"//颜色串扰crosstalk程度

    init {
        includeList.add(ColorSpaceConversion)
        includeList.add(ConstantParams)
    }

    override val code: String
        get() = """
        #define $paramSigma 0.05//高光去饱和调整程度
        #define $paramAlpha 0.04//减少高光去饱和计算色度的干扰程度
        

       float chroma_correction_bt2446(float L, float Lref, float Lmax, float $paramSigma) {
           if (L > Lref) {
               return max(1.0 - $paramSigma * (L - Lref) / (Lmax - Lref), 0.0);
           }
           return 1.0;
       }

       vec3 crosstalk(vec3 x, float a) {
           float b = 1.0 - 2.0 * a;
           mat3  M = mat3(
               b, a, a,
               a, b, a,
               a, a, b);
           return x * M;
       }

       vec3 crosstalk_inv(vec3 x, float a) {
           float b = 1.0 - a;
           float c = 1.0 - 3.0 * a;
           mat3  M = mat3(
                b, -a, -a,
               -a,  b, -a,
               -a, -a,  b) / c;
           return x * M;
       }

       vec4 ${javaClass.name}(vec4 color) {
           const float L_ref = $methodBt2020ToLab(vec3(1.0)).x;
           const float L_max = $methodBt2020ToLab(vec3($MAX_FRAME_AVERAGE_LUMINANCE / $HDR_REFERENCE_WHITE)).x;
           color.rgb = crosstalk(color.rgb, $paramAlpha);
           color.rgb = $methodBt2020ToLab(color.rgb);
           color.rgb = $methodLabToLch(color.rgb);
           color.y  *= chroma_correction_bt2446(color.x, L_ref, L_max, $paramSigma);
           color.rgb = $methodLchToLab(color.rgb);
           color.rgb = $methodLabToBT2020(color.rgb);
           color.rgb = crosstalk_inv(color.rgb, $paramAlpha);
           return color;
       }
        """.trimIndent()
}