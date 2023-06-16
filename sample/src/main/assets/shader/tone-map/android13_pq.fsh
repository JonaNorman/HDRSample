//参考地址：https://android.googlesource.com/platform/frameworks/native/+/refs/heads/master/libs/tonemap/tonemap.cpp
#include shader/gamma/pq.fsh
#include shader/gamma/bt709.fsh
#include shader/colorspace/color_gamut.fsh

uniform vec2 displayLuminanceRange;// 屏幕最大亮度
uniform float maxFrameAverageLuminance;// 最大平均亮度



float OETFTone(float channel) {
    channel = channel / PQ_MAX_LUMINANCE;
    return PQ_EOTF_1(channel);
}

float toneMapTargetNits(float maxRGB) {
    float maxInLumi = maxFrameAverageLuminance;
    float maxOutLumi = displayLuminanceRange.y;
    float nits = maxRGB;
    float x1 = maxOutLumi * 0.65;
    float y1 = x1;
    float x3 = maxInLumi;
    float y3 = maxOutLumi;
    float x2 = x1 + (x3 - x1) * 4.0 / 17.0;
    float y2 = maxOutLumi * 0.9;
    float greyNorm1 = OETFTone(x1);
    float greyNorm2 = OETFTone(x2);
    float greyNorm3 = OETFTone(x3);
    float slope1 = 0;
    float slope2 = (y2 - y1) / (greyNorm2 - greyNorm1);
    float slope3 = (y3 - y2) / (greyNorm3 - greyNorm2);
    if (nits < x1) {
        return nits;
    }
    if (nits > maxInLumi) {
        return maxOutLumi;
    }
    float greyNits = OETFTone(nits);
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


void main()
{
    vec4 rgba  = textureColor();
    vec3 linearRGB = PQ_EOTF(rgba.rgb);
    vec3 xyz = BT2020_TO_XYZ(linearRGB);
    vec3 absoluteRGB = linearRGB *PQ_MAX_LUMINANCE;
    vec3 absoluteXYZ = xyz *PQ_MAX_LUMINANCE;
    float gain = lookupTonemapGain(absoluteRGB);
    xyz = absoluteXYZ * gain/displayLuminanceRange.y;
    vec3 finalColor = BT709_OETF(XYZ_TO_BT709(xyz));
    setOutColor(vec4(finalColor, rgba.a));
}









