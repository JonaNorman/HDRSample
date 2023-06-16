//参考代码: https://github.com/VoidXH/Cinema-Shader-Pack/blob/master/Shaders/HDR%20to%20SDR.hlsl

#include shader/gamma/pq.fsh
#include shader/gamma/bt709.fsh
#include shader/colorspace/color_gamut.fsh

#define KNEE  0.75
#define COMPRESSOR 1.0

uniform vec2 displayLuminanceRange;// 屏幕最大亮度
uniform float maxFrameAverageLuminance;// 最大平均亮度

float minGain(vec3 pixel) {
    return min(pixel.r, min(pixel.g, pixel.b));
}

float midGain(vec3 pixel) {
    return pixel.r < pixel.g ?
    (pixel.r < pixel.b ?
    min(pixel.g, pixel.b) :// min = r
    min(pixel.r, pixel.g)) :// min = b
    (pixel.g < pixel.b ?
    min(pixel.r, pixel.b) :// min = g
    min(pixel.r, pixel.g));// min = b
}

float maxGain(vec3 pixel) {
    return max(pixel.r, max(pixel.g, pixel.b));
}

vec3 compress(vec3 pixel) {
    float gain = maxGain(pixel);
    return pixel * (gain < KNEE ? gain : KNEE + max(gain - KNEE, 0) * COMPRESSOR) / gain;
}

vec3 fixClip(vec3 pixel) {
    // keep the (mid - min) / (max - min) ratio
    float preMin = minGain(pixel);
    float preMid = midGain(pixel);
    float preMax = maxGain(pixel);
    vec3 clip = saturate(pixel);
    float postMin = minGain(clip);
    float postMid = midGain(clip);
    float postMax = maxGain(clip);
    float ratio = (preMid - preMin) / (preMax - preMin);
    float newMid = ratio * (postMax - postMin) + postMin;
    return vec3(clip.r != postMid ? clip.r : newMid,
    clip.g != postMid ? clip.g : newMid,
    clip.b != postMid ? clip.b : newMid);
}



void main()
{
    vec4 rgba  = textureColor();
    vec3 linearRGB = PQ_EOTF(rgba.rgb);
    linearRGB =linearRGB* PQ_MAX_LUMINANCE / maxFrameAverageLuminance.y;
    linearRGB = BT2020_TO_BT709(linearRGB);
    vec3 finalColor = BT709_OETF(compress(linearRGB));
    finalColor = fixClip(finalColor);
    setOutColor(vec4(finalColor, rgba.a));
}