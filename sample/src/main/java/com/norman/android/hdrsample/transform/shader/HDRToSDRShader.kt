package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode
import com.norman.android.hdrsample.player.VideoOutput.ColorSpace
import com.norman.android.hdrsample.transform.shader.MetaDataParams.COLOR_SPACE_BT2020_LINEAR
import com.norman.android.hdrsample.transform.shader.chromacorrect.ChromaCorrection
import com.norman.android.hdrsample.transform.shader.gamma.GammaEOTF
import com.norman.android.hdrsample.transform.shader.gamma.GammaOETF
import com.norman.android.hdrsample.transform.shader.gamutmap.GamutMap
import com.norman.android.hdrsample.transform.shader.tonemap.ToneMap

class HDRToSDRShader(
    @ColorSpace colorSpace: Int,
    chromaCorrection: ChromaCorrection,
    toneMap: ToneMap,
    gamutMap: GamutMap,
    gammaOETF: GammaOETF
) : GLShaderCode() {

    @JvmField
    val chromaCorrection: ChromaCorrection

    @JvmField
    val gamutMap: GamutMap

    @JvmField
    val toneMap: ToneMap

    @JvmField
    val colorSpace: Int

    @JvmField
    val gammaOETF: GammaOETF

    val gammaEOTF: GammaEOTF


    init {
        this.chromaCorrection = chromaCorrection
        this.gamutMap = gamutMap
        this.toneMap = toneMap
        this.colorSpace = colorSpace
        this.gammaOETF = gammaOETF;
        this.gammaEOTF = if (colorSpace == ColorSpace.VIDEO_BT2020_PQ) {
            GammaEOTF.PQDisPlay
        } else if (colorSpace == ColorSpace.VIDEO_BT2020_HLG) {
            GammaEOTF.HLGDisplay
        } else {
            GammaEOTF.NONE
        }
    }

    override val code: String
        get() =
            """
            |precision highp float;
            |varying highp vec2 textureCoordinate;
            |uniform sampler2D inputImageTexture;
            |#define ${MetaDataParams.VIDEO_COLOR_SPACE}  $colorSpace
            |
            |${MetaDataParams.code}
            |
            |${gammaOETF.code}
            |
            |${gammaEOTF.code}
            |
            |${ColorConversion.code}
            |
            |${ReScale.code}
            |
            |${chromaCorrection.code}
            |
            |${gamutMap.code}
            |
            |${toneMap.code}
            |
            |void main()
            |{
            |  vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
            |  vec3 rgb = textureColor.rgb;
            |  vec3 linearColor = ${gammaEOTF.methodGamma}(rgb);
            |  linearColor = ${chromaCorrection.methodChromaCorrect}(linearColor);
            |  if($colorSpace != ${COLOR_SPACE_BT2020_LINEAR}){
            |     linearColor= ${toneMap.methodToneMap}(linearColor);
            |  }
            |  vec3 gamutMapColor = ${gamutMap.methodGamutMap}(linearColor);
            |  vec3 finalColor = ${gammaOETF.methodGamma}(gamutMapColor);
            |  gl_FragColor.rgb = finalColor;
            |  gl_FragColor.a = textureColor.a;
            |}
            """.trimMargin()

}