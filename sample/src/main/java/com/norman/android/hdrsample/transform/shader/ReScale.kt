package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode
import com.norman.android.hdrsample.transform.shader.MetaDataParams.COLOR_SPACE_BT2020_HLG
import com.norman.android.hdrsample.transform.shader.MetaDataParams.COLOR_SPACE_BT2020_PQ
import com.norman.android.hdrsample.transform.shader.MetaDataParams.HDR_REFERENCE_WHITE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.HLG_MAX_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.MAX_DISPLAY_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.PQ_MAX_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.VIDEO_COLOR_SPACE

/**
 * Gamma矫正后的颜色是归一化的，而不同的ToneMap要求的颜色有些是绝对亮度，有些是相对参考白电平的值，所以需要先放大做ToneMap，做完ToneMap再缩放回来
 * 参考https://cs.android.com/android/platform/superproject/+/master:frameworks/native/libs/renderengine/gl/ProgramCache.cpp?q=NormalizeLuminance
 */
object ReScale : GLShaderCode() {

    val methodScaleToMaster = "SCALE_TO_MASTER"
    val methodNormalizeMaster =  "NORMALIZE_MASTER"
    val methodNormalizeDisplay =  "NORMALIZE_DISPLAY"
    val methodScaleReferenceWhiteToOne =  "SCALE_ONE_TO_REFERENCE_WHITE"

    override val code: String
        get() = """
                vec3 $methodScaleToMaster(vec3 color){
                    if($VIDEO_COLOR_SPACE == $COLOR_SPACE_BT2020_PQ){
                        return color*$PQ_MAX_LUMINANCE;
                    }else if($VIDEO_COLOR_SPACE == $COLOR_SPACE_BT2020_HLG){
                        return color*$HLG_MAX_LUMINANCE;
                    }
                    return color; 
                }
                
                float $methodNormalizeMaster(float color){
                    if($VIDEO_COLOR_SPACE == $COLOR_SPACE_BT2020_PQ){
                        return color/$PQ_MAX_LUMINANCE;
                    }else if($VIDEO_COLOR_SPACE == $COLOR_SPACE_BT2020_HLG){
                        return color/$HLG_MAX_LUMINANCE;
                    }
                    return color; 
                }
                
                 vec3 $methodNormalizeMaster(vec3 color){
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
                
                 vec3 $methodScaleReferenceWhiteToOne(vec3 color){
                     if($VIDEO_COLOR_SPACE == $COLOR_SPACE_BT2020_PQ){
                        return color*$PQ_MAX_LUMINANCE/$HDR_REFERENCE_WHITE;
                    }else if($VIDEO_COLOR_SPACE == $COLOR_SPACE_BT2020_HLG){
                        return color*$HLG_MAX_LUMINANCE/$HDR_REFERENCE_WHITE;
                    }
                    return color; 
                }
                
               
            """.trimIndent()
}