package com.norman.android.hdrsample.todo;

import android.media.MediaFormat;

import com.norman.android.hdrsample.util.DisplayUtil;
import com.norman.android.hdrsample.util.MediaFormatUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class AndroidTexturePlayerRenderer {

//
//  const val OES_FRAGMENT_SHADER = """#extension GL_OES_EGL_image_external : require
//                precision mediump float;
//                varying highp vec2 textureCoordinate;
//                uniform samplerExternalOES inputImageTexture;
//
//                void main()
//                {
//                    vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
//                    mat3 tonemap = mat3(1.6605, -0.1246, -0.0182,
//                    -0.5876,  1.1329, -0.1006,
//                   -0.0728, -0.0083,  1.1187);
//                   vec3 tonemapColor = tonemap * textureColor.rgb;
//                    gl_FragColor = vec4(tonemapColor.rgb,textureColor.a);
//                }"""


//    const val EXT_2DY2Y_FRAGMENT_SHADER = """#version 300 es
//#extension GL_OES_EGL_image_external : require
//#extension GL_EXT_YUV_target : require
//
//precision highp float;
//uniform __samplerExternal2DY2YEXT inputImageTexture;
//uniform float screenLuminance;
//uniform float contentLuminance;
//in  vec2 textureCoordinate;
//out vec4 outColor;
//
//#define COLOR_STANDARD_BT709 1
//#define COLOR_STANDARD_BT2020 6
//
//#define COLOR_TRANSFER_LINEAR 1
//#define COLOR_TRANSFER_SDR_VIDEO  3
//#define COLOR_TRANSFER_ST2084 6
//#define COLOR_TRANSFER_HLG 7
//
//#define ST2084_M1       0.1593017578125
//#define ST2084_M2       78.84375
//#define ST2084_C1       0.8359375
//#define ST2084_C2       18.8515625
//#define ST2084_C3       18.6875
//#define ST2084_PEAK_LUMINANCE  10000.0
//#define ST2084_REFERENCE_WHITE 100.0
//#define FLT_MIN        1.17549435082228750797e-38
//
//#define BT709_ALPHA    1.09929682680944
//#define BT709_BETA     0.018053968510807
//
//
//#define BT2020_TO_BT709_MAT3  mat3(1.660491, -0.12455047, -0.01815076, \
//                                                -0.58764114, 1.1328999, -0.1005789, \
//                                                -0.07284986, -0.00834942, 1.11872966)
//
//#define YUV_TO_RGB_MAT4    mat4(1.167808, 1.167808, 1.167808, 0.0, \
//                                                0.0, -0.187877, 2.148072, 0.0, \
//                                                1.683611, -0.652337, 0.000000, 0.0, \
//                                                -0.915688, 0.347458, -1.148145, 1.0)
//
//
//vec3 yuvToRgb(vec3 yuv){
//    vec4 color = YUV_TO_RGB_MAT4 *vec4(yuv.xyz, 1.0);
//    return color.rgb;
//}
//
//float st2084EOTF(float x)
//{
//    if (x > 0.0) {
//        float xpow = pow(x, 1.0 / ST2084_M2);
//        float num = max(xpow - ST2084_C1, 0.0);
//        float den = max(ST2084_C2 - ST2084_C3 * xpow, FLT_MIN);
//        return pow(num / den, 1.0 / ST2084_M1);
//    } else {
//        return 0.0;
//    }
//}
//
//vec3 st2084EOTF(vec3 rgb)
//{
//    return vec3(st2084EOTF(rgb.r), st2084EOTF(rgb.g), st2084EOTF(rgb.b));
//}
//
//float bt709OETF(float x)
//{
//    x = max(x, 0.0);
//    if (x < BT709_BETA){
//        x = x * 4.5;
//    } else {
//        x = BT709_ALPHA * pow(x, 0.45) - (BT709_ALPHA - 1.0);
//    }
//    return x;
//}
//
//vec3 bt709OETF(vec3 rgb)
//{
//    return vec3(bt709OETF(rgb.r), bt709OETF(rgb.g), bt709OETF(rgb.b));
//}
//
//vec3 bt2020Tobt709(vec3 x)
//{
//    return BT2020_TO_BT709_MAT3 * x;
//}
//
//float hable(float v)
//{
//    float a = 0.15, b = 0.50, c = 0.10, d = 0.20, e = 0.02, f = 0.30;
//    return (v * (v * a + b * c) + d * e) / (v * (v * a + b) + d * f) - e / f;
//}
//
//vec3 hable(vec3 rgb){
//    return vec3(hable(rgb.x), hable(rgb.y), hable(rgb.z));
//}
//
//float maxVec3(vec3 color){
//    return max(max(color.r, color.g), color.b);
//}
//
//
//vec3 zimgTonemap(vec3 rgb){
//    vec3 linearColor = st2084EOTF(rgb);
//    float scale = ST2084_PEAK_LUMINANCE / contentLuminance;
//    float sig_orig = max(maxVec3(rgb), 1e-6);
//    float peak = screenLuminance/ ST2084_REFERENCE_WHITE;
//    float sig = hable(sig_orig) / hable(peak);
//    vec3 scaleColor =  linearColor * scale * sig / sig_orig;
//    vec3 bt709Color = bt2020Tobt709(scaleColor);
//    return bt709OETF(bt709Color);
//}
//
//vec3 tonemap(vec3 rgb){
//    return zimgTonemap(rgb);
//}
//
//void main()
//{
//    vec4 yuv  = texture(inputImageTexture, textureCoordinate);
//    vec3 rgb =  yuvToRgb(yuv.rgb);
//    vec3 tonemapColor = tonemap(rgb);
//    outColor = vec4(tonemapColor, 1.0);
//}
//            """
    private float contentLuminance;
    private float screenLuminance;

    boolean keepBrightnessOnHDR;


    public AndroidTexturePlayerRenderer() {


        screenLuminance = DisplayUtil.getMaxLuminance();

    }

    public synchronized void setKeepBrightnessOnHDR(boolean keepBrightnessOnHDR) {
        ScreenBrightnessObserver screenBrightnessObserver = new ScreenBrightnessObserver();

        this.keepBrightnessOnHDR = keepBrightnessOnHDR;
        if (keepBrightnessOnHDR) {
            screenBrightnessObserver.listen();
        } else {
            screenBrightnessObserver.unListen();
        }
        float brightness = 1;
        if (keepBrightnessOnHDR) {
            ScreenBrightnessObserver.BrightnessInfo brightnessInfo = screenBrightnessObserver.getBrightnessInfo();
            brightness = brightnessInfo.brightnessFloat;
        }

    }

    protected void onOutputFormatChanged(MediaFormat outputFormat) {
        ByteBuffer hdrStaticInfo = MediaFormatUtil.getByteBuffer(outputFormat, MediaFormat.KEY_HDR_STATIC_INFO);
        if (hdrStaticInfo != null) {
            hdrStaticInfo.clear();
            hdrStaticInfo.position(1);
            hdrStaticInfo.limit(hdrStaticInfo.capacity());
            hdrStaticInfo.order(ByteOrder.LITTLE_ENDIAN);
            ShortBuffer shortBuffer = hdrStaticInfo.asShortBuffer();
            int maxFrameAverageLuminance = shortBuffer.get(11);
        }

    }

}
