package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.transform.shader.MetaDataParams.COLOR_SPACE_BT2020_HLG
import com.norman.android.hdrsample.transform.shader.MetaDataParams.COLOR_SPACE_BT2020_PQ
import com.norman.android.hdrsample.transform.shader.MetaDataParams.HLG_MAX_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.MAX_DISPLAY_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.PQ_MAX_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.VIDEO_COLOR_SPACE

/**
 * Gamma矫正后的颜色是归一化的，需要缩放成亮度绝对值，方便后续ToneMap后归一化
 * 参考https://cs.android.com/android/platform/superproject/+/master:frameworks/native/libs/renderengine/gl/ProgramCache.cpp?q=NormalizeLuminance
 */
object ReScaleSDR : ReScale() {


    override val code: String
        get() = """
                vec3 $methodScaleAbsolute(vec3 color){
                    if($VIDEO_COLOR_SPACE == $COLOR_SPACE_BT2020_PQ){
                        return color*$PQ_MAX_LUMINANCE;
                    }else if($VIDEO_COLOR_SPACE == $COLOR_SPACE_BT2020_HLG){
                        return color*$HLG_MAX_LUMINANCE;
                    }
                    return color; 
                }
                
                float $methodScaleNormalize(float color){
                    if($VIDEO_COLOR_SPACE == $COLOR_SPACE_BT2020_PQ){
                        return color/$PQ_MAX_LUMINANCE;
                    }else if($VIDEO_COLOR_SPACE == $COLOR_SPACE_BT2020_HLG){
                        return color/$HLG_MAX_LUMINANCE;
                    }
                    return color; 
                }
                
                 vec3 $methodScaleNormalize(vec3 color){
                    if($VIDEO_COLOR_SPACE == $COLOR_SPACE_BT2020_PQ){
                        return color/$PQ_MAX_LUMINANCE;
                    }else if($VIDEO_COLOR_SPACE == $COLOR_SPACE_BT2020_HLG){
                        return color/$HLG_MAX_LUMINANCE;
                    }
                    return color; 
                }
                
                vec3 $methodNormalizeDisplay(vec3 color){
                     return color / $MAX_DISPLAY_LUMINANCE;
                }
            """.trimIndent()
}