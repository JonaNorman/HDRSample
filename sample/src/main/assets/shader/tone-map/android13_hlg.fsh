
//参考地址：https://android.googlesource.com/platform/frameworks/native/+/refs/heads/master/libs/tonemap/tonemap.cpp
#include shader/gamma/hlg.fsh
#include shader/gamma/bt709.fsh
#include shader/colorspace/color_gamut.fsh

uniform vec2 displayLuminanceRange;// 屏幕最大亮度
uniform float maxFrameAverageLuminance;// 最大平均亮度


#include HLG_GAMMA_VALUE = HLG_GAMMA(displayLuminanceRange.y)

float toneMapTargetNits(float maxRGB) {
    return maxRGB
    * pow(maxRGB / HLG_MAX_LUMINANCE, HLG_GAMMA_VALUE - 1.0)
    * displayLuminanceRange.y / HLG_MAX_LUMINANCE;
}

float lookupTonemapGain(vec3 linearRGB, vec3 xyz) {
    float maxRGB = max(linearRGB.r, max(linearRGB.g, linearRGB.b));
    if (maxRGB <= 0.0) {
        return 1.0;
    }
    return toneMapTargetNits(maxRGB) / maxRGB;
}


void main()
{
    vec4 rgba  = textureColor();
    vec3 linearRGB = HLG_EOTF(rgba.rgb,displayLuminanceRange);
    vec3 xyz = BT2020_TO_XYZ(linearRGB);
    vec3 absoluteRGB = linearRGB *HLG_MAX_LUMINANCE;
    vec3 absoluteXYZ = xyz *HLG_MAX_LUMINANCE;
    float gain = lookupTonemapGain(absoluteRGB);
    xyz = absoluteXYZ * gain/displayLuminanceRange.y;
    vec3 finalColor = BT709_OETF(XYZ_TO_BT709(xyz));
    setOutColor(vec4(finalColor, rgba.a));
}


