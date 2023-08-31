package com.norman.android.hdrsample.player.shader

import androidx.annotation.IntDef
import com.norman.android.hdrsample.opengl.GLShaderCode
import com.norman.android.hdrsample.player.color.YUV420Type

/**
 *
 * YUV420有四种格式
 * YV12 三个平面依次 Y平面 V平面 U平面，YUV三个平面的宽一样，U平面和V平面的高都是Y平面的1/4
 * YV21 三个平面依次 Y平面 U平面 V平面，YUV三个平面的宽一样，U平面和V平面的高都是Y平面的1/4
 * NV12 两个平面依次 Y平面 UV平面，UV平面里面是按UU间隔，U平面和V平面的宽高都是Y平面的1/2
 * NV21 两个平面依次 Y平面 VU平面，VU平面里面是按VU间隔，U平面和V平面的宽高都是Y平面的1/2
 *
 * 总结：https://juejin.cn/post/7207637337572606007
 */
class YUV420FragmentShader(@YUV420Type type: Int) : GLShaderCode() {

    val yuv420Type = type;

    override val code = """
            #version 300 es
            precision highp float;
            precision highp int;

            uniform highp usampler2D $LUMA_TEXTURE;//Y平面
            uniform highp usampler2D $CHROMA_SEMI_TEXTURE;//NV21和NV12的UV平面，因为两个数据是在一起的，所以合在一个纹理里
            uniform highp usampler2D $CHROMA_PLANAR_U_TEXTURE;//YV12和YV12的U平面
            uniform highp usampler2D $CHROMA_PLANAR_V_TEXTURE;//YV12和YV12的V平面

            uniform vec2 $LUMA_SIZE;//Y平面的大小
            uniform vec2 $CHROMA_PLANAR_U_SIZE;//YV12和YV12的U平面大小
            uniform vec2 $CHROMA_PLANAR_V_SIZE;//YV12和YV12的V平面大小
            uniform vec2 $CHROMA_SEMI_SIZE;//NV21和NV12的UV平面大小

            uniform mat4 $YUV_TO_RGB_MATRIX;//YUV转RGB矩阵
            uniform int $BIT_DEPTH;//深度
            uniform int $BIT_MASK;//10位纹理其实是16位位移后的数据，所以需要位移回去，8位是0，10位是6

            in  vec2 textureCoordinate;
            out vec4 outColor;

            #define MAX_COLOR_VALUE  (pow(2.0,float($BIT_DEPTH))-1.0)//位深决定颜色的最大值，8位是255，10是1023，注意是0开始的所以要减去1

            vec3 yuvToRgb(vec3 yuv){// yuv转RGB
                vec4 color = $YUV_TO_RGB_MATRIX *vec4(yuv, 1.0);
                return color.rgb;
            }

            //usampler2D获取的颜色是无符号量化的数据，需要归一化
            //矩阵已经处理了范围问题，不需要再特殊处理直接除以最大值就够了，譬如10位limit range不是除以940而是除以1023
            float normalizedColor(uint color){//归一化，向右位移多余的位数除以最大值
                return float(color>>$BIT_MASK)/MAX_COLOR_VALUE;
            }

            vec2 normalizedColor(uvec2 color){
                return vec2(normalizedColor(color.x),normalizedColor(color.y));
            }

            ivec2 quantizedCoord(vec2 coord, vec2 size){//coord坐标是归一化的，usampler2D访问纹理需要用实际的尺寸大小
                return ivec2(coord*(size-1.0)+0.5);//不直接乘以size是因为个人觉得纹理访问其实取的是中间值
            }

            float getLumaColor(vec2 textureCoord){
                uint color = texelFetch($LUMA_TEXTURE, quantizedCoord(textureCoord, $LUMA_SIZE), 0).x;
                return normalizedColor(color);
            }

            vec2 getChromaSemiColor(vec2 textureCoord){
                uvec2 color = texelFetch($CHROMA_SEMI_TEXTURE, quantizedCoord(textureCoord, $CHROMA_SEMI_SIZE), 0).xy;
                return normalizedColor(color);
            }

            float getChromaPlanarUColor(vec2 textureCoord){
                uint color = texelFetch($CHROMA_PLANAR_U_TEXTURE, quantizedCoord(textureCoord, $CHROMA_PLANAR_U_SIZE), 0).x;
                return normalizedColor(color);
            }


            float getChromaPlanarVColor(vec2 textureCoord){
                uint color = texelFetch($CHROMA_PLANAR_V_TEXTURE, quantizedCoord(textureCoord, $CHROMA_PLANAR_V_SIZE), 0).x;
                return normalizedColor(color);
            }
            
            void main() {
                vec3 yuv = vec3(0.0);
                ${
                when (yuv420Type) {
                    YUV420Type.YV21, YUV420Type.YV12 -> """
                            float y = getLumaColor(textureCoordinate);
                            float u =  getChromaPlanarUColor(textureCoordinate);
                            float v =  getChromaPlanarVColor(textureCoordinate);
                            yuv = vec3(y, u, v);
                            """.trimIndent()
        
                    YUV420Type.NV12 -> """
                            float y = getLumaColor(textureCoordinate);
                            vec2 uv =  getChromaSemiColor(textureCoordinate);
                            yuv =vec3(y, uv);
                            """.trimIndent()
        
                    YUV420Type.NV21 -> """
                             float y = getLumaColor(textureCoordinate);
                             vec2 vu =  getChromaSemiColor(textureCoordinate);
                             yuv= vec3(y, vu.yx);//vu变成uv需要换一下xy
                             """.trimIndent()
        
                    else -> ""
                }}      
                outColor.rgb =yuvToRgb(yuv);
                outColor.a = 1.0;
            }
    """.trimIndent()


    companion object {
        /**
         * Y平面
         */
        const val LUMA_TEXTURE = "lumaTexture"

        /**
         * NV21和NV12的UV平面，因为两个数据是在一起的，所以合在一个纹理里
         */
        const val CHROMA_SEMI_TEXTURE = "chromaSemiTexture"

        /**
         * YV12和YV12的U平面
         */
        const val CHROMA_PLANAR_U_TEXTURE = "chromaPlanarUTexture"

        /**
         * YV12和YV12的V平面
         */
        const val CHROMA_PLANAR_V_TEXTURE = "chromaPlanarVTexture"

        /**
         * Y平面的大小
         */

        const val LUMA_SIZE = "lumaSize"

        /**
         * YV12和YV12的U平面大小
         */
        const val CHROMA_PLANAR_U_SIZE = "chromaPlanarUSize"

        /**
         * YV12和YV12的V平面大小
         */
        const val CHROMA_PLANAR_V_SIZE = "chromaPlanarVSize"

        /**
         * NV21和NV12的UV平面大小
         */
        const val CHROMA_SEMI_SIZE = "chromaSemiSize"

        /**
         * YUV转RGB矩阵
         */

        const val YUV_TO_RGB_MATRIX = "yuvToRgbMatrix"

        /**
         * 深度
         */

        const val BIT_DEPTH = "bitDepth"

        /**
         *10位纹理其实是16位位移后的数据，所以需要位移回去，8位时bitMask是0，10位时bitmask是6
         */

        const val BIT_MASK = "bitMask"
    }

}