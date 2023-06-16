//参考代码:https://github.com/FFmpeg/FFmpeg/blob/master/libavfilter/vf_tonemap.c
// https://blog.csdn.net/a360940265a/article/details/124671992

uniform vec2 displayLuminanceRange;// 屏幕最大亮度
uniform float maxFrameAverageLuminance;// 最大平均亮度

#include shader/gamma/hlg.fsh
#include shader/gamma/bt709.fsh
#include shader/colorspace/color_gamut.fsh

float hable(float v)
{
    float a = 0.15, b = 0.50, c = 0.10, d = 0.20, e = 0.02, f = 0.30;
    return (v * (v * a + b * c) + d * e) / (v * (v * a + b) + d * f) - e / f;
}


void main() {

    vec4 rgba  = textureColor();
    vec3 linearColor = HLG_EOTF(rgba.rgb,displayLuminanceRange);
    float scale = HLG_MAX_LUMINANCE / maxFrameAverageLuminance;//2.5
    float sig_orig = max(max(rgba.r, rgba.g), rgba.b);
    float peak = displayLuminanceRange.y/ HLG_REFERENCE_WHITE;
    float sig = hable(sig_orig) / hable(peak);
    vec3 scaleColor =  linearColor * scale * sig / sig_orig;
    vec3 bt709Color = BT2020_TO_BT709(scaleColor);//
    vec3 tonemapColor  =  BT709_OETF(bt709Color);//
    setOutColor(vec4(tonemapColor, rgba.a));
}





