package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode
import com.norman.android.hdrsample.player.color.ColorSpace
import com.norman.android.hdrsample.transform.shader.MetaDataParams.COLOR_SPACE_BT2020_LINEAR
import com.norman.android.hdrsample.transform.shader.chromacorrect.ChromaCorrection
import com.norman.android.hdrsample.transform.shader.gamma.GammaEOTF
import com.norman.android.hdrsample.transform.shader.gamma.GammaOETF
import com.norman.android.hdrsample.transform.shader.gamutmap.GamutMap
import com.norman.android.hdrsample.transform.shader.tonemap.ToneMap

/**
 * HDR转SDR的Shader，
 */
class HDRToSDRShader(
    @ColorSpace colorSpace: Int,
    chromaCorrection: ChromaCorrection,
    toneMap: ToneMap,
    gamutMap: GamutMap,
    gammaOETF: GammaOETF,
    referenceDisplay: Boolean
) : GLShaderCode() {

    /**
     * 色度矫正，处理高光转SDR后的色调变化问题
     */
    @JvmField
    val chromaCorrection: ChromaCorrection

    /**
     * 色域映射，BT2020转BT709
     */
    @JvmField
    val gamutMap: GamutMap

    /**
     * 色调映射，为了亮度降低后色调基本不变
     */

    @JvmField
    val toneMap: ToneMap

    /**
     * 视频内的颜色空间
     */

    @JvmField
    val colorSpace: Int


    /**
     * 转换成SDR内容的Gamma压缩
     */
    @JvmField
    val gammaOETF: GammaOETF

    /**
     * 把输入的HDR视频内容转换线性
     */
    val gammaEOTF: GammaEOTF


    init {
        this.chromaCorrection = chromaCorrection
        this.gamutMap = gamutMap
        this.toneMap = toneMap
        this.colorSpace = colorSpace
        this.gammaOETF = gammaOETF
        //显示参考和场景参考对HDR转SDR的内容有影响
        this.gammaEOTF = if (colorSpace == ColorSpace.VIDEO_BT2020_PQ) {
            if (referenceDisplay) GammaEOTF.PQDisPlay else GammaEOTF.PQScene
        } else if (colorSpace == ColorSpace.VIDEO_BT2020_HLG) {
            if (referenceDisplay) GammaEOTF.HLGDisplay else GammaEOTF.HLGScene
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
            |  vec3 linearColor = ${gammaEOTF.methodGamma}(rgb);//转成线性
            |  linearColor = ${chromaCorrection.methodChromaCorrect}(linearColor);//色度矫正
            |  if($colorSpace != ${COLOR_SPACE_BT2020_LINEAR}){
            |     linearColor= ${toneMap.methodToneMap}(linearColor);//色调映射
            |  }
            |  vec3 gamutMapColor = ${gamutMap.methodGamutMap}(linearColor);//色域转换
            |  vec3 finalColor = ${gammaOETF.methodGamma}(gamutMapColor);//gamma压缩
            |  gl_FragColor.rgb = finalColor;
            |  gl_FragColor.a = textureColor.a;
            |}
            """.trimMargin()

}