package com.norman.android.hdrsample.transform.shader.chromacorrect

import com.norman.android.hdrsample.transform.shader.ColorConversion.methodBt2020ToLab
import com.norman.android.hdrsample.transform.shader.ColorConversion.methodLabToBT2020
import com.norman.android.hdrsample.transform.shader.ColorConversion.methodLabToLch
import com.norman.android.hdrsample.transform.shader.ColorConversion.methodLchToLab
import com.norman.android.hdrsample.transform.shader.MetaDataParams.HDR_REFERENCE_WHITE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.HDR_PEAK_LUMINANCE
import com.norman.android.hdrsample.transform.shader.ReScale

/**
 * 色度矫正，该方法是BT2446C方法介绍，对超过HDR参考白的高光去饱和避免压缩高光导致的色调偏移
 * 步骤1: crosstalk矩阵解决后续高光去饱和计算色度的干扰问题，个人觉得是经验值，alpha从0-0.5
 * 步骤2: RGB转换成LAB(L亮度、A绿色到红色分量、B蓝色到黄色分量)，LAB再转换LCH(L亮度、C色度、H色调)
 * 步骤3: 对HDR参考白以上颜色LCH的C通道(色度)进行降色处理，规则是亮度越高色度越低
 * 步骤4: 还原成RGB颜色
 * https://www.itu.int/dms_pub/itu-r/opb/rep/R-REP-BT.2446-1-2021-PDF-E.pdf
 * https://github.com/natural-harmonia-gropius/hdr-toys/blob/master/utils/chroma_correction.glsl
 */
class ChromaCorrectionBT2446C : ChromaCorrection() {

    override val code: String
        get() = """
        #define CHROMA_CORRECT_STRENGTH 0.05//高光去饱和调整程度
        #define CROSSTALK_STRENGTH 0.04 //减少颜色rgb里色度的串扰程度 0~0.33
        
        // 这个公式在文档中有
        float chroma_correction(float L, float Lref, float Lmax) {
           if (L > Lref) {//大于参考白表示高光，返回的值表示饱和度，亮度L越大饱和度越小
               return max(1.0 - CHROMA_CORRECT_STRENGTH * (L - Lref) / (Lmax - Lref), 0.0);
           }
           return 1.0;
        }
        
        vec3 crosstalk(vec3 x) {
           float a = CROSSTALK_STRENGTH;
           float b = 1.0 - 2.0 * a;
           mat3  M = mat3(
               b, a, a,
               a, b, a,
               a, a, b);
           return x * M;
        }
        vec3 crosstalk_inv(vec3 x) {
           float a = CROSSTALK_STRENGTH;
           float b = 1.0 - a;
           float c = 1.0 - 3.0 * a;
           mat3  M = mat3(
                b, -a, -a,
               -a,  b, -a,
               -a, -a,  b) / c;
           return x * M;
        }

        // 注意输入的颜色不是0～1，而0～MAX_CONTENT_LUMINANCE，大于HDR_REFERENCE_WHITE是高光颜色
        vec3 ${methodChromaCorrect}(vec3 color) {
           color = ${ReScale.methodScaleToMaster}(color);
           float L_ref = $methodBt2020ToLab(vec3($HDR_REFERENCE_WHITE)).x;
           float L_max = $methodBt2020ToLab(vec3($HDR_PEAK_LUMINANCE)).x;
           color = crosstalk(color);
           color = $methodBt2020ToLab(color);
           color = $methodLabToLch(color);
           color.y  *= chroma_correction(color.x, L_ref, L_max);
           color = $methodLchToLab(color);
           color = $methodLabToBT2020(color);
           color = crosstalk_inv(color);
           color = ${ReScale.methodNormalizeMaster}(color);
           return color;
        }
        """.trimIndent()
}