package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode
import com.norman.android.hdrsample.transform.shader.MetaDataParams.HDR_REFERENCE_WHITE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.HLG_MAX_LUMINANCE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.PQ_MAX_LUMINANCE

class HDRToSDR(chromaCorrection: ChromaCorrection, gamutMap: GamutMap, toneMap: ToneMap) : GLShaderCode() {

    val chromaCorrection: ChromaCorrection
    val gamutMap: GamutMap
    val toneMap: ToneMap

    init {
        this.chromaCorrection = chromaCorrection
        this.gamutMap = gamutMap
        this.toneMap = toneMap
    }

    override val code: String
        get() = """
            precision highp float;
            varying highp vec2 textureCoordinate;
            uniform sampler2D inputImageTexture;
            
            ${MetaDataParams.code}
            ${ColorConversion.code}
            ${GammaHLG.code}
            ${GammaPQ.code}
            ${GammaBT709.code}
            ${chromaCorrection.code}
            ${toneMap.code}
            ${gamutMap.code}
            
            vec3 scaleLinear(vec3 color) {
               if(${MetaDataParams.VIDEO_COLOR_SPACE} == ${MetaDataParams.COLOR_SPACE_BT2020_PQ}){
                    return color*$HLG_MAX_LUMINANCE/$HDR_REFERENCE_WHITE;
               }else if(${MetaDataParams.VIDEO_COLOR_SPACE} == ${MetaDataParams.COLOR_SPACE_BT2020_HLG}){
                    return color*$PQ_MAX_LUMINANCE/$HDR_REFERENCE_WHITE;
               }
               return color;         
            }
            
          
            void main()
            {
              vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
              vec3 rgb = textureColor.rgb;
              vec3 linearColor;
              if(${MetaDataParams.VIDEO_COLOR_SPACE} == ${MetaDataParams.COLOR_SPACE_BT2020_HLG}){
                   linearColor = ${GammaHLG.methodHLGEOTF}(rgb);
              }else if(${MetaDataParams.VIDEO_COLOR_SPACE} == ${MetaDataParams.COLOR_SPACE_BT2020_PQ}){
                   linearColor = ${GammaPQ.methodPQEOTF}(rgb);
              }
              vec3 chromaCorrectColor = ${chromaCorrection.methodChromaCorrect}(linearColor);
              vec3 toneMapColor = ${toneMap.methodToneMap}(chromaCorrectColor);
              vec3 gamutMapColor = ${gamutMap.methodGamutMap}(toneMapColor);
              vec3 finalColor = ${GammaBT709.methodBt709OETF}(gamutMapColor);
              gl_FragColor.rgb = finalColor;
              gl_FragColor.a = textureColor.a;
            }
            
        """.trimIndent()

}