//https://github.com/toru-ver4/sample_code/blob/develop/ty_lib/bt2446_method_c.py
// BT2446-C的曲线可视化 https://www.desmos.com/calculator/rv08vuzqjk?lang=zh-CN
// BT2446-C的曲线可视化https://www.desmos.com/calculator/1dwlw3ultd?lang=ru
//https://trev16.hatenablog.com/entry/2020/08/01/131907

//https://github.com/natural-harmonia-gropius/hdr-toys


#include shader/gamma/pq.fsh
#include shader/gamma/bt709.fsh
#include shader/colorspace/color_gamut.fsh

const float ip = 0.58535;// linear length
const float k1 = 0.83802;// linear strength
const float k3 = 0.74204;// shoulder strength

float f(float Y, float k1, float k3, float ip) {
    ip /= k1;
    float k2 = (k1 * ip) * (1.0 - k3);
    float k4 = (k1 * ip) - (k2 * log(1.0 - k3));
    return Y < ip ?
    Y * k1 :
    log((Y / ip) - k3) * k2 + k4;
}

float curve(float x) {
    const float over_white = 1019.0 / 940.0;// 109% range (super-whites)
    return f(x, k1, k3, ip) / over_white;
}

vec3 tonemap(vec3 rgb) {
    vec3 xyz = BT2020_TO_XYZ(rgb);
    vec3 xyY = XYZ_TO_xyY(xyz);
    float Y   = curve(xyY.z);
    xyz = xyY_to_XYZ(vec3(xyY.x, xyY.y, Y));
    return XYZ_TO_BT2020(xyz);
}

void main()
{
    vec4 hdr = textureColor();
    vec3 rgb_linear = PQ_EOTF(hdr.rgb)*PQ_MAX_LUMINANCE;
    rgb_linear = clamp(rgb_linear, 0.0, PQ_MAX_LUMINANCE);

    /*
     * Apply the EETF with the maxRGB method to map the PQ signal with a peak
     * luminance of 10000 cd/m² to 1000 cd/m² (HLG), ITU-R BT.2408-5 annex 5
     */
    float m1 = max(rgb_linear.r, max(rgb_linear.g, rgb_linear.b));
    float m2 = PQ_EOTF(pq_eetf(pq_oetf(m1/PQ_MAX_LUMINANCE)))*PQ_MAX_LUMINANCE;
    rgb_linear *= m2 / m1;

    /* Rescale the PQ signal so [0, 1000] maps to [0, 1] */
    rgb_linear /= 1000.0;

    vec3 sdr = BT2020_TO_BT709(tonemap(rgb_linear));
    setOutColor(vec4(sdr, hdr.a));
}