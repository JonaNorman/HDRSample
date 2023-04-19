#define     ST2084_M1       0.1593017578125
#define     ST2084_M2       78.84375
#define     ST2084_C1       0.8359375
#define     ST2084_C2       18.8515625
#define     ST2084_C3       18.6875
#define     FLT_MIN         1.17549435082228750797e-38
#define     REC709_ALPHA    1.09929682680944
#define     REC709_BETA     0.018053968510807

precision highp float;
uniform sampler2D inputImageTexture;
varying highp vec2 textureCoordinate;
uniform float max_screen_luminance;
uniform float max_peak_luminance;

highp float st_2084_eotf(highp float x)
{
    if (x > 0.0) {
        highp float xpow = pow(x, float(1.0 / ST2084_M2));
        highp float num = max(xpow - ST2084_C1, 0.0);
        highp float den = max(ST2084_C2 - ST2084_C3 * xpow, FLT_MIN);
        return pow(num / den, 1.0 / ST2084_M1);
    } else {
        return 0.0;
    }
}

float hable(float v)
{
    float a = 0.15, b = 0.50, c = 0.10, d = 0.20, e = 0.02, f = 0.30;
    return (v * (v * a + b * c) + d * e) / (v * (v * a + b) + d * f) - e / f;
}

vec3 hable(vec3 rgb){
    return vec3(hable(rgb.x), hable(rgb.y), hable(rgb.z));
}


highp float rec_709_oetf(highp float x)
{
    x = max(x, 0.0);
    if (x < REC709_BETA)
    x = x * 4.5;
    else
    x = REC709_ALPHA * pow(x, 0.45) - (REC709_ALPHA - 1.0);
    return x;
}
highp vec3 rec_709_oetf(vec3 rgb)
{

    return vec3(rec_709_oetf(rgb.x), rec_709_oetf(rgb.y), rec_709_oetf(rgb.z));
}


float max3(float a, float  b, float c){
    return max(max(a, b), c);
}


    #define ngli_linear(a, b, x) (((x) - (a)) / ((b) - (a)))
    #define ngli_sat(x) clamp(x, 0.0, 1.0)


const vec3 luma_coeff = vec3(0.2627, 0.6780, 0.0593);// luma weights for BT.2020
const float l_hdr = 1000.0;
const float l_sdr = 100.0;
const float p_hdr = 1.0 + 32.0 * pow(l_hdr / 10000.0, 1.0 / 2.4);
const float p_sdr = 1.0 + 32.0 * pow(l_sdr / 10000.0, 1.0 / 2.4);
const float gcr = luma_coeff.r / luma_coeff.g;
const float gcb = luma_coeff.b / luma_coeff.g;

/* BT.2446-1-2021 method A */
vec3 tonemap(vec3 x)
{
    vec3 xp = pow(x, vec3(1.0 / 2.4));
    float y_hdr = dot(luma_coeff, xp);

    /* Step 1: convert signal to perceptually linear space */
    float yp = log(1.0 + (p_hdr - 1.0) * y_hdr) / log(p_hdr);

    /* Step 2: apply knee function in perceptual domain */
    float yc = mix(
    1.077 * yp,
    mix((-1.1510 * yp + 2.7811) * yp - 0.6302, 0.5 * yp + 0.5, yp > 0.9909),
    yp > 0.7399);

    /* Step 3: convert back to gamma domain */
    float y_sdr = (pow(p_sdr, yc) - 1.0) / (p_sdr - 1.0);

    /* Colour correction */
    float scale = y_sdr / (1.1 * y_hdr);
    float cb_tmo = scale * (xp.b - y_hdr);
    float cr_tmo = scale * (xp.r - y_hdr);
    float y_tmo = y_sdr - max(0.1 * cr_tmo, 0.0);

    /* Convert from Y'Cb'Cr' to R'G'B' (still in BT.2020) */
    float cg_tmo = -(gcr * cr_tmo + gcb * cb_tmo);
    return y_tmo + vec3(cr_tmo, cg_tmo, cb_tmo);
}

vec3 bt2020_to_bt709(vec3 x)
{
    const mat3 bt2020_to_bt709 = mat3(
    1.660491, -0.12455047, -0.01815076,
    -0.58764114, 1.1328999, -0.1005789,
    -0.07284986, -0.00834942, 1.11872966);
    return bt2020_to_bt709 * x;
}


/* ITU-R BT.2100 */
const float pq_m1 = 0.1593017578125;
const float pq_m2 = 78.84375;
const float pq_c1 = 0.8359375;
const float pq_c2 = 18.8515625;
const float pq_c3 = 18.6875;

/* PQ Reference EOTF (linearize: R'G'B' HDR → RGB HDR), ITU-R BT.2100 */
vec3 pq_eotf3(vec3 x)
{
    vec3 p = pow(x, vec3(1.0 / pq_m2));
    vec3 num = max(p - pq_c1, 0.0);
    vec3 den = pq_c2 - pq_c3 * p;
    vec3 Y = pow(num / den, vec3(1.0 / pq_m1));
    return 10000.0 * Y;
}

float pq_eotf(float x)
{
    return pq_eotf3(vec3(x)).x;
}

/* PQ Reference OETF (EOTF¯¹), ITU-R BT.2100 */
float pq_oetf(float x)
{
    float Y = x / 10000.0;
    float Ym = pow(Y, pq_m1);
    return pow((pq_c1 + pq_c2 * Ym) / (1.0 + pq_c3 * Ym), pq_m2);
}

/*
 * Entire PQ encoding luminance range. Could be refined if mastering display
 * Lb/Lw are known.
 */
const float Lb = 0.0;       /* minimum black luminance */
const float Lw = 10000.0;   /* peak white luminance */

/*
 * Target HLG luminance range.
 */
const float Lmin = 0.0;
const float Lmax = 1000.0;

/* EETF (non-linear PQ signal → non-linear PQ signal), ITU-R BT.2408-5 annex 5 */
float pq_eetf(float x)
{
    /* Step 1 */
    float v_min = pq_oetf(Lb);
    float v_max = pq_oetf(Lw);
    float e1 = ngli_linear(v_min, v_max, x);

    float l_min = pq_oetf(Lmin);
    float l_max = pq_oetf(Lmax);
    float min_lum = ngli_linear(v_min, v_max, l_min);
    float max_lum = ngli_linear(v_min, v_max, l_max);

    /* Step 2 */
    float ks = 1.5 * max_lum - 0.5; /* knee start (roll off beginning) */
    float b = min_lum;

    /* Step 4: Hermite spline P(t) */
    float t = ngli_linear(ks, 1.0, e1);
    float t2 = t * t;
    float t3 = t2 * t;
    float p = (2.0 * t3 - 3.0 * t2 + 1.0) * ks
    + (t3 - 2.0 * t2 + t) * (1.0 - ks)
    + (-2.0 * t3 + 3.0 * t2) * max_lum;

    /* Step 3: solve for the EETF (e3) with given end points */
    float e2 = mix(p, e1, step(e1, ks));

    /*
     * Step 4: the following step is supposed to be defined for 0 ≤E₂≤ 1 but no
     * alternative outside is given, so assuming we need to clamp
     */
    e2 = ngli_sat(e2);
    float e3 = e2 + b * pow(1.0 - e2, 4.0);

    /*
     * Step 5: invert the normalization of the PQ values based on the mastering
     * display black and white luminances, Lb and Lw, to obtain the target
     * display PQ values.
     */
    float e4 = mix(v_min, v_max, e3);
    return e4;
}


vec3 sdr_to3(vec3 hdr){
    vec3 rgb_linear = pq_eotf3(hdr.rgb);
    rgb_linear = clamp(rgb_linear, 0.0, 10000.0);

    /*
     * Apply the EETF with the maxRGB method to map the PQ signal with a peak
     * luminance of 10000 cd/m² to 1000 cd/m² (HLG), ITU-R BT.2408-5 annex 5
     */
    float m1 = max(rgb_linear.r, max(rgb_linear.g, rgb_linear.b));
    float m2 = pq_eotf(pq_eetf(pq_oetf(m1)));
    rgb_linear *= m2 / m1;

    /* Rescale the PQ signal so [0, 1000] maps to [0, 1] */
    rgb_linear /= 1000.0;

    vec3 sdr = bt2020_to_bt709(tonemap(rgb_linear));
    return sdr;
}


vec3 zimgtonmp(vec3 rgb){
    highp float sig;
    highp float sig_orig;
    sig = max(max3(rgb.r, rgb.g, rgb.b), 1e-6);
    sig_orig = sig;
    float peak = 300.0/ 100.0;// 手机设备的最大亮度值MaxCLL / REFERENCE_WHITE(固定100);
    sig = hable(sig) / hable(peak);
    vec3 color;
    color.r = rgb.r * (sig / sig_orig);
    color.g = rgb.g * (sig / sig_orig);
    color.b = rgb.b * (sig / sig_orig);
    return color;
}

void main() {

    highp vec4 rgb10bit =  texture2D(inputImageTexture, textureCoordinate);
    // 第一步、电 转线性光信号
    float ST2084_PEAK_LUMINANCE = 10000.0;
    // 参考zscale源代码参数ST2084_PEAK_LUMINANCE固定1w，peak_luminance视频元数据-标峰亮度值，静态元数据的全视频用一个peak，动态元数据的每一帧一个peak
    // 参考zscale源代码to_linear的GammaOperationC，to_linear_scale赋值是postscale，后处理缩放。
    float to_linear_scale = ST2084_PEAK_LUMINANCE / 300.0;
    highp vec3 fragColor = to_linear_scale * vec3(st_2084_eotf(rgb10bit.r), st_2084_eotf(rgb10bit.g), st_2084_eotf(rgb10bit.b));


    vec3 sdr1 = fragColor;
    // 第二步、HDR线性 ToneMapping映射转成 SDR线性
    // 参考ffmpeg的tonemap函数，hable算法。

    sdr1 = zimgtonmp(sdr1);

    // 第三步、逆线性光信号，变回电
    // 参考zscale源代码，rec_709的to_gamma，没有prescale和postscale.
    sdr1 = rec_709_oetf(sdr1);

    vec3 sdr2 = fragColor;

    sdr2 = zimgtonmp(sdr2);

    sdr2 = rec_709_oetf(bt2020_to_bt709(sdr2));


    vec3 sdr3 = sdr_to3(rgb10bit.rgb);

    vec3 sdr4 = bt2020_to_bt709(rgb10bit.rgb);


    gl_FragColor = vec4(abs(sdr2-sdr1), 1.0);
}

