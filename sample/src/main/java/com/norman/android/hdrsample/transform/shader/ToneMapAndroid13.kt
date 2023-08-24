package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.transform.shader.ColorConversion.methodBt2020ToXYZ
import com.norman.android.hdrsample.transform.shader.ColorConversion.methodXYZToBt2020
import com.norman.android.hdrsample.transform.shader.MetaDataParams.COLOR_SPACE_BT2020_HLG
import com.norman.android.hdrsample.transform.shader.MetaDataParams.HLG_MAX_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.MAX_CONTENT_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.MAX_DISPLAY_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.PQ_MAX_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.VIDEO_COLOR_SPACE

/**
 * Android13的实现
 * 个人理解
 * HLG是直接按屏幕最大亮度进行缩放
 * PQ是把输入的值按照给定几个点插值形成的曲线进行调整，插值按PQEOTF进行拟合
 * (x1,y1) x1=y1等于屏幕最大亮度的0.65
 * (x2,y2) x2表示x1和x3之间的4.0/17.0  y2表示屏幕亮度最大亮度的0.9
 * (x3,y3) x3等于调整前的最大亮度，y3等于调整后的最大亮度即屏幕最大亮度
 */
//参考地址：https://android.googlesource.com/platform/frameworks/native/+/refs/heads/master/libs/tonemap/tonemap.cpp

object ToneMapAndroid13 : ToneMap() {
    override val code: String
        get() = """
            float toneMapTargetNits(float maxRGB) {
          
                if($VIDEO_COLOR_SPACE == $COLOR_SPACE_BT2020_HLG){//Android原来中有用hlgGamma调整，但是我们这边的OETF已经已经进行OOTF调整，不需要再做一次
                    return maxRGB * $MAX_DISPLAY_LUMINANCE / $HLG_MAX_LUMINANCE;
                }
                float maxInLumi = $MAX_CONTENT_LUMINANCE;
                float maxOutLumi = $MAX_DISPLAY_LUMINANCE;
                float nits = maxRGB;
                float x1 = maxOutLumi * 0.65;
                float y1 = x1;
                float x3 = maxInLumi;
                float y3 = maxOutLumi;
                float x2 = x1 + (x3 - x1) * 4.0 / 17.0;
                float y2 = maxOutLumi * 0.9;
                vec3 greyNorm = ${GammaPQ.methodOETF}(vec3(x1,x2,x3) / $PQ_MAX_LUMINANCE);
                float greyNorm1 = greyNorm.x;//Android用的PQOETF其实是PQEOTF的逆函数，这里改成改成真正的OETF也许是对的
                float greyNorm2 = greyNorm.y;
                float greyNorm3 = greyNorm.z;
                float slope1 = 0.0;
                float slope2 = (y2 - y1) / (greyNorm2 - greyNorm1);
                float slope3 = (y3 - y2) / (greyNorm3 - greyNorm2);
                if (nits < x1) {
                    return nits;
                }
                if (nits > maxInLumi) {
                    return maxOutLumi;
                }
                float greyNits = ${GammaPQ.methodOETF}(nits / $PQ_MAX_LUMINANCE);
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

        vec3 $methodToneMap(vec3 rgb)
        {
            vec3 xyz = $methodBt2020ToXYZ(rgb);
            float gain = lookupTonemapGain(rgb);//maxRgb用曲线调整后的比值作为gain值
            xyz = xyz * gain;
            return $methodXYZToBt2020(xyz);
        }
        """.trimIndent()
}