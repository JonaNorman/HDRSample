//参考代码：https://github.com/libretro/RetroArch/blob/master/gfx/drivers/vulkan_shaders/hdr.frag

#include shader/gamma/pq.fsh
#include shader/gamma/bt709.fsh
#include shader/colorspace/color_gamut.fsh

uniform vec2 displayLuminanceRange;// 屏幕最大亮度
uniform float maxFrameAverageLuminance;// 最大平均亮度


#define kEpsilon            0.0001
#define kLumaChannelRatio   0.25
#define CONTRAST            2.0


vec3 InverseTonemap(vec3 sdr)
{
    vec3 hdr;

    sdr = pow(abs(sdr), vec3(CONTRAST / 2.2f));       /* Display Gamma - needs to be determined by calibration screen */

    float luma = dot(sdr, vec3(0.2126, 0.7152, 0.0722));  /* Rec BT.709 luma coefficients - https://en.wikipedia.org/wiki/Luma_(video) */

    /* Inverse reinhard tonemap */
    float maxValue             = (displayLuminanceRange.y / PQ_REFERENCE_WHITE) + kEpsilon;
    float elbow                = maxValue / (maxValue - 1.0f);
    float offset               = 1.0f - ((0.5f * elbow) / (elbow - 0.5f));

    float hdrLumaInvTonemap    = offset + ((luma * elbow) / (elbow - luma));
    float sdrLumaInvTonemap    = luma / ((1.0f + kEpsilon) - luma);                     /* Convert the srd < 0.5 to 0.0 -> 1.0 range */

    float lumaInvTonemap       = (luma > 0.5f) ? hdrLumaInvTonemap : sdrLumaInvTonemap;
    vec3 perLuma               = sdr / (luma + kEpsilon) * lumaInvTonemap;

    vec3 hdrInvTonemap         = offset + ((sdr * elbow) / (elbow - sdr));
    vec3 sdrInvTonemap         = sdr / ((1.0f + kEpsilon) - sdr);                       /* Convert the srd < 0.5 to 0.0 -> 1.0 range */

    vec3 perChannel            = vec3(sdr.x > 0.5f ? hdrInvTonemap.x : sdrInvTonemap.x,
    sdr.y > 0.5f ? hdrInvTonemap.y : sdrInvTonemap.y,
    sdr.z > 0.5f ? hdrInvTonemap.z : sdrInvTonemap.z);

    hdr = mix(perLuma, perChannel, vec3(kLumaChannelRatio));

    return hdr;
}


vec3 Hdr10(vec3 hdr)
{
    vec3 rec2020 = BT709_TO_BT2020(hdr);
    vec3 linearColour = rec2020 * (PQ_REFERENCE_WHITE/ PQ_MAX_LUMINANCE);
    return  PQ_EOTF_1(linearColour);
}

void main()
{
    vec4 rgba  = textureColor();
    vec3 hdr =Hdr10(InverseTonemap(rgba.rgb));
    setOutColor(vec4(finalColor, rgba.a));
}