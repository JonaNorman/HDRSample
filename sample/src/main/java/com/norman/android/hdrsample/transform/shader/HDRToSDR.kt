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
            ${ReScaleSDR.code}
            ${GammaHLG.code}
            ${GammaPQ.code}
            ${GammaBT709.code}
            ${chromaCorrection.code}
            ${toneMap.code}
            ${gamutMap.code}
            
            void main()
            {
              vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
              vec3 rgb = textureColor.rgb;
              vec3 linearColor;
              if(${MetaDataParams.VIDEO_COLOR_SPACE} == ${MetaDataParams.COLOR_SPACE_BT2020_HLG}){
                   linearColor = ${GammaHLG.methodHLGEOTF}(rgb);
              }else if(${MetaDataParams.VIDEO_COLOR_SPACE} == ${MetaDataParams.COLOR_SPACE_BT2020_PQ}){
                   linearColor = ${GammaPQ.methodPQEOTF}(rgb);
              }else{
                   gl_FragColor = vec4(1.0,0.0,0.0,1.0);
                   return;
              }
              vec3 scaleColor = ${ReScaleSDR.methodScale}(linearColor);
              vec3 chromaCorrectColor = ${chromaCorrection.methodChromaCorrect}(scaleColor);
              vec3 toneMapColor = ${toneMap.methodToneMap}(chromaCorrectColor);
              vec3 normalizeColor = ${ReScaleSDR.methodNormalize}(toneMapColor);
              vec3 gamutMapColor = ${gamutMap.methodGamutMap}(normalizeColor);
              vec3 finalColor = ${GammaBT709.methodBt709OETF}(gamutMapColor);
              gl_FragColor.rgb = finalColor;
              gl_FragColor.a = textureColor.a;
            }
            
        """.trimIndent()

}