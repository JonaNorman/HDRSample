package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.transform.shader.MetaDataParams.COLOR_SPACE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.COLOR_SPACE_BT2020_HLG
import com.norman.android.hdrsample.transform.shader.MetaDataParams.COLOR_SPACE_BT2020_PQ
import com.norman.android.hdrsample.transform.shader.MetaDataParams.MAX_DISPLAY_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.MAX_FRAME_AVERAGE_LUMINANCE
import com.norman.android.hdrsample.transform.shader.ColorSpaceConversion.methodBt2020ToXYZ
import com.norman.android.hdrsample.transform.shader.ColorSpaceConversion.methodXYZToBt2020
import com.norman.android.hdrsample.transform.shader.ConstantParams.HLG_MAX_LUMINANCE
import com.norman.android.hdrsample.transform.shader.ConstantParams.PQ_MAX_LUMINANCE
import com.norman.android.hdrsample.transform.shader.GammaHLG.methodHLGGamma
import com.norman.android.hdrsample.transform.shader.GammaPQ.methodPQEOTFInv
import com.norman.android.hdrsample.transform.shader.MetaDataParams.CURRENT_DISPLAY_LUMINANCE

//参考地址：https://android.googlesource.com/platform/frameworks/native/+/refs/heads/master/libs/tonemap/tonemap.cpp

object ToneMapAndroid13 : ToneMap() {
    override val code: String
        get() = """
            
            vec3 ScaleLuminance(vec3 xyz) {
               return $COLOR_SPACE == $COLOR_SPACE_BT2020_PQ?xyz * $PQ_MAX_LUMINANCE:xyz * $HLG_MAX_LUMINANCE;         
            }
        
            vec3 NormalizeLuminance(vec3 xyz) {
              return xyz / $MAX_DISPLAY_LUMINANCE;
            }

            float toneMapTargetNits(float maxRGB) {
                if($COLOR_SPACE == $COLOR_SPACE_BT2020_HLG){
                    return maxRGB * pow(maxRGB / $HLG_MAX_LUMINANCE, $methodHLGGamma($CURRENT_DISPLAY_LUMINANCE) - 1.0) * $MAX_DISPLAY_LUMINANCE / $HLG_MAX_LUMINANCE;
                }
                float maxInLumi = $MAX_FRAME_AVERAGE_LUMINANCE;
                float maxOutLumi = $MAX_DISPLAY_LUMINANCE;
                float nits = maxRGB;
                float x1 = maxOutLumi * 0.65;
                float y1 = x1;
                float x3 = maxInLumi;
                float y3 = maxOutLumi;
                float x2 = x1 + (x3 - x1) * 4.0 / 17.0;
                float y2 = maxOutLumi * 0.9;
                float greyNorm1 = $methodPQEOTFInv(x1 / $PQ_MAX_LUMINANCE);
                float greyNorm2 = $methodPQEOTFInv(x2 / $PQ_MAX_LUMINANCE);
                float greyNorm3 = $methodPQEOTFInv(x3 / $PQ_MAX_LUMINANCE);
                float slope1 = 0;
                float slope2 = (y2 - y1) / (greyNorm2 - greyNorm1);
                float slope3 = (y3 - y2) / (greyNorm3 - greyNorm2);
                if (nits < x1) {
                    return nits;
                }
                if (nits > maxInLumi) {
                    return maxOutLumi;
                }
                float greyNits = $methodPQEOTFInv(nits / PQ_MAX_LUMINANCE);
                if (greyNits <= greyNorm2) {
                    nits = (greyNits - greyNorm2) * slope2 + y2;
                } else if (greyNits <= greyNorm3) {
                    nits = (greyNits - greyNorm3) * slope3 + y3;
                } else {
                    nits = maxOutLumi;
                }
                return nits;
            }

            float lookupTonemapGain(vec3 linearRGB) {
                float maxRGB = max(linearRGB.r, max(linearRGB.g, linearRGB.b));
                if (maxRGB <= 0.0) {
                    return 1.0;
                }
                return toneMapTargetNits(maxRGB) / maxRGB;
            }

        vec3 $methodToneMap(vec3 rgb,vec3 xyz)
        {
            vec3 xyz = $methodBt2020ToXYZ(rgb)
            vec3 absoluteRGB = ScaleLuminance(rgb);
            vec3 absoluteXYZ = ScaleLuminance(xyz);
            float gain = lookupTonemapGain(absoluteRGB);
            xyz = NormalizeLuminance(absoluteXYZ * gain);
            return $methodXYZToBt2020(xyz);
        }
        """.trimIndent()
}