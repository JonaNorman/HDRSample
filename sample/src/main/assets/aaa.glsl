precision highp float;
varying highp vec2 textureCoordinate;
uniform sampler2D inputImageTexture;

uniform float MIN_DISPLAY_LUMINANCE;// 最大屏幕亮度
uniform float MAX_DISPLAY_LUMINANCE;// 最大屏幕亮度
uniform float CURRENT_DISPLAY_LUMINANCE;// 当前亮度
uniform float MAX_CONTENT_LUMINANCE;// 最大亮度
uniform int VIDEO_COLOR_SPACE;// 颜色空间
#define COLOR_SPACE_BT2020_PQ 1
#define COLOR_SPACE_BT2020_HLG 2
#define PI  3.1415926 //圆周率
#define EPSILON  1e-6 // 精度阙值
#define HDR_REFERENCE_WHITE 203.0  //HDR参考白亮度
#define HLG_MAX_LUMINANCE 1000.0  //HLG最大亮度
#define PQ_MAX_LUMINANCE 10000.0  //PQ最大亮度
#define BT2020_TO_BT709_MAT3  mat3(1.660491,-0.12455047,-0.01815076, \
                                      -0.58764114,1.1328999,-0.1005789, \
                                      -0.07284986,-0.00834942,1.11872966)

#define BT709_TO_BT2020_MAT3  mat3(0.6274040,0.0690970,0.0163916, \
                                      0.3292820,0.9195400,0.0880132, \
                                      0.0433136,0.0113612,0.8955950)

#define BT2020_TO_XYZ_MAT3  mat3(0.636958,0.262700,0.000000, \
                                    0.144617,0.677998,0.028073, \
                                    0.168881,0.059302,1.060985)


#define XYZ_TO_BT2020_MAT3  mat3(1.716651,-0.666684,0.017640, \
                                   -0.355671,1.616481,-0.042771, \
                                   -0.253366,0.015769,0.942103)

#define XYZ_TO_BT709_MAT3  mat3(3.240970,-0.969244,0.055630, \
                                   -1.537383,1.875968,-0.203977, \
                                   -0.498611,0.041555, 1.056972)

#define XYZD65_TO_XYZD50_MAT3  mat3(1.047930,0.029628,-0.009243,\
                                       0.022947,0.990434,0.015055,\
                                       -0.050192,-0.017074,0.751874)


#define XYZD50_TO_XYZD65_MAT3  mat3(0.955473, -0.028370,0.012314,\
                                        -0.023099,1.009995,-0.020508,\
                                        0.063259,0.021041,1.330366)

#define  LAB_DELTA  6.0 / 29.0
#define  LAB_DELTAC  LAB_DELTA * 2.0 / 3.0

vec3 BT2020_TO_BT709(vec3 x)
{
    return BT2020_TO_BT709_MAT3 * x;
}

vec3 BT709_TO_BT2020(vec3 x)
{
    return BT709_TO_BT2020_MAT3 * x;
}

vec3 XYZ_TO_BT709(vec3 x)
{
    return XYZ_TO_BT709_MAT3 * x;
}

vec3 BT2020_TO_XYZ(vec3 x)
{
    return BT2020_TO_XYZ_MAT3 * x;
}

vec3 XYZ_TO_BT2020(vec3 x)
{
    return XYZ_TO_BT2020_MAT3 * x;
}

vec3 XYZD65_TO_XYZD50(vec3 x) {

    return XYZD65_TO_XYZD50_MAT3 * x;
}

vec3 XYZD50_TO_XYZD65(vec3 x) {
    return XYZD50_TO_XYZD65_MAT3 * x;
}


float labf1(float x) {
    return x > pow(LAB_DELTA, 3.0) ?
    sign(x) * pow(abs(x), 1.0 / 3.0) :
    LAB_DELTAC + x / (3.0 * pow(LAB_DELTA, 2.0));
}

float labf2(float x) {
    return x > LAB_DELTA ?
    pow(x, 3.0) :
    (x - LAB_DELTAC) * (3.0 * pow(LAB_DELTA, 2.0));
}

vec3 XYZ_TO_LAB(vec3 XYZ,vec3 XYZ_ref) {
    float X = XYZ.x;
    float Y = XYZ.y;
    float Z = XYZ.z;
    X = labf1(X / XYZ_ref.x);
    Y = labf1(Y / XYZ_ref.y);
    Z = labf1(Z / XYZ_ref.z);
    float L = 116.0 * Y - 16.0;
    float a = 500.0 * (X - Y);
    float b = 200.0 * (Y - Z);
    return vec3(L, a, b);
}

vec3 LAB_TO_XYZ(vec3 Lab,vec3 XYZ_ref) {
    float L = Lab.x;
    float a = Lab.y;
    float b = Lab.z;
    float Y = (L + 16.0) / 116.0;
    float X = Y + a / 500.0;
    float Z = Y - b / 200.0;
    X = labf2(X) * XYZ_ref.x;
    Y = labf2(Y) * XYZ_ref.y;
    Z = labf2(Z) * XYZ_ref.z;
    return vec3(X, Y, Z);
}


vec3 BT2020_TO_LAB(vec3 color) {
    color  = BT2020_TO_XYZ(color);
    color  = XYZD65_TO_XYZD50(color);
    color  = XYZ_TO_LAB(color,BT2020_TO_XYZ(vec3(HDR_REFERENCE_WHITE)));
    return color;
}

vec3 LAB_TO_BT2020(vec3 color) {
    color  = LAB_TO_XYZ(color,BT2020_TO_XYZ(vec3(HDR_REFERENCE_WHITE)));
    color  = XYZD50_TO_XYZD65(color);
    color  = XYZ_TO_BT2020(color);
    return color;
}

vec3 LAB_to_LCH(vec3 Lab) {
    float a = Lab.y;
    float b = Lab.z;
    float C = length(vec2(a, b));
    float H = 0.0;
    if (!(abs(a) < EPSILON && abs(b) < EPSILON)) {
        H = atan(b, a);
        H = H * 180.0 / PI;
        H = mod((mod(H, 360.0) + 360.0), 360.0);
    }
    return vec3(Lab.x, C, H);
}

vec3 LCH_to_LAB(vec3 LCH) {
    float C = max(LCH.y, 0.0);
    float H = LCH.z * PI / 180.0;
    float a = C * cos(H);
    float b = C * sin(H);
    return vec3(LCH.x, a, b);
}

vec3 XYZ_TO_xyY(vec3 XYZ) {
    float divisor = XYZ.x + XYZ.y + XYZ.z;
    if (divisor == 0.0) divisor = 1e-6;
    float x = XYZ.x / divisor;
    float y = XYZ.y / divisor;
    return vec3(x, y, XYZ.z);
}

vec3 xyY_TO_XYZ(vec3 xyY) {
    float multiplo = xyY.z / max(xyY.y, 1e-6);
    float z = 1.0 - xyY.x - xyY.y;
    float X = xyY.x * multiplo;
    float Z = z * multiplo;
    return vec3(X, xyY.z, Z);
}
vec3 RESCALE_ABSOLUTE(vec3 color){
    if(VIDEO_COLOR_SPACE == COLOR_SPACE_BT2020_PQ){
        return color*PQ_MAX_LUMINANCE;
    }else if(VIDEO_COLOR_SPACE == COLOR_SPACE_BT2020_HLG){
        return color*HLG_MAX_LUMINANCE;
    }
    return color;
}

float RESCALE_NORMALIZE(float color){
    if(VIDEO_COLOR_SPACE == COLOR_SPACE_BT2020_PQ){
        return color/PQ_MAX_LUMINANCE;
    }else if(VIDEO_COLOR_SPACE == COLOR_SPACE_BT2020_HLG){
        return color/HLG_MAX_LUMINANCE;
    }
    return color;
}

vec3 RESCALE_NORMALIZE(vec3 color){
    if(VIDEO_COLOR_SPACE == COLOR_SPACE_BT2020_PQ){
        return color/PQ_MAX_LUMINANCE;
    }else if(VIDEO_COLOR_SPACE == COLOR_SPACE_BT2020_HLG){
        return color/HLG_MAX_LUMINANCE;
    }
    return color;
}

vec3 NORMALIZE_DISPLAY(vec3 color){
    return color / MAX_DISPLAY_LUMINANCE;
}
#define  HLG_A  0.17883277// ABC三个参数是为了平滑连接HLG的两端曲线
#define  HLG_B  0.28466892
#define  HLG_C  0.55991073
#define  HLG_MIN_BRIGHTNESS_NITS 500.0// 防止HLG亮度过低时黑暗场景过于明亮，参考至Android的computeHlgGamma做法


vec3 HLG_OETF(vec3 x)
{
    return mix(sqrt(3.0 * x),
    HLG_A * log(12.0 * x - HLG_B) + HLG_C,
    step(1.0 / 12.0, x));
}

// OETF的逆函数
vec3 HLG_OETF_1(vec3 x)
{
    return mix(x * x / 3.0,
    (HLG_B+exp((x - HLG_C) / HLG_A)) / 12.0,
    step(0.5, x));
}

// HLG的系统伽马，根据设备亮度调整，1000亮度时候系统伽马是1.2，
float HLG_GAMMA(float lw){
    lw = max(lw,HLG_MIN_BRIGHTNESS_NITS);
    return 1.2+0.42*log(lw/HLG_MAX_LUMINANCE)/log(10.0);
}

vec3 HLG_OOTF(vec3 x)
{
    return x * pow(BT2020_TO_XYZ(x).y,HLG_GAMMA(MAX_DISPLAY_LUMINANCE)-1.0);
}

vec3 HLG_BLACK_LIFT(vec3 x){//调整黑电平，range表示亮度的范围，x表示最小，y表示最大
    float b = sqrt(3.0 * pow(MIN_DISPLAY_LUMINANCE/MAX_DISPLAY_LUMINANCE, 1.0/HLG_GAMMA(MAX_DISPLAY_LUMINANCE)));
    return max(vec3(0.0), (1.0-b)*x + b);
}

vec3 HLG_EOTF(vec3 x)
{
    return HLG_OOTF(HLG_OETF_1(HLG_BLACK_LIFT(x)));
}
#define PQ_M1  0.1593017578125
#define PQ_M2  78.84375
#define PQ_C1  0.8359375
#define PQ_C2  18.8515625
#define PQ_C3  18.6875



// EOTF的逆函数
vec3 PQ_EOTF_1(vec3 x)
{
    vec3 Y = x ;
    vec3 Ym = pow(Y, vec3(PQ_M1));
    return pow((PQ_C1 + PQ_C2 * Ym) / (1.0 + PQ_C3 * Ym), vec3(PQ_M2));
}

float PQ_EOTF_1(float x)
{
    float Y = x ;
    float Ym = pow(Y, PQ_M1);
    return pow((PQ_C1 + PQ_C2 * Ym) / (1.0 + PQ_C3 * Ym), PQ_M2);
}

vec3 PQ_OOTF(vec3 x){
    vec3 x1 =  mix(267.84*x, 1.099*pow(59.5208*x,vec3(0.45))-0.099, step(0.0003024, x));
    return 100.0* pow(x1,vec3(2.4));
}

vec3 PQ_OETF(vec3 x){
    return PQ_EOTF_1(PQ_OOTF(x));
}

vec3 PQ_EOTF(vec3 x)
{
    vec3 p = pow(x, vec3(1.0 / PQ_M2));
    vec3 num = max(p - PQ_C1, 0.0);
    vec3 den = PQ_C2 - PQ_C3 * p;
    vec3 Y = pow(num / den, vec3(1.0 / PQ_M1));
    return  Y;
}
#define BT709_ALPHA    1.09929682680944//ALPHA和BETA是为了平滑直线和曲线算出来的
#define BT709_BETA     0.018053968510807
#define BT709_GAMMA    1.0/0.45
#define BT709_GAMMA_INV    0.45

vec3 BT709_OETF(vec3 x)
{
    return mix(x * 4.5, BT709_ALPHA * pow(x, vec3(BT709_GAMMA_INV)) - (BT709_ALPHA - 1.0), step(BT709_BETA, x));
}

vec3 BT709_EOTF(vec3 x)
{
    return mix(x / 4.5,   pow((x + (BT709_ALPHA - 1.0)) / BT709_ALPHA, vec3(BT709_GAMMA)), step(BT709_BETA*4.5, x));
}
#define CHROMA_CORRECT_STRENGTH 0.05//高光去饱和调整程度
#define CROSSTALK_STRENGTH 0.04 //减少颜色rgb里色度的串扰程度 0~0.33

// 这个公式在文档中有
float chroma_correction(float L, float Lref, float Lmax) {
    if (L > Lref) {//大于参考白表示高光，返回的值表示饱和度，亮度L越大饱和度越小
        return max(1.0 - CHROMA_CORRECT_STRENGTH * (L - Lref) / (Lmax - Lref), 0.0);
    }
    return 1.0;
}

vec3 crosstalk(vec3 x) {
    float a = CROSSTALK_STRENGTH;
    float b = 1.0 - 2.0 * a;
    mat3  M = mat3(
    b, a, a,
    a, b, a,
    a, a, b);
    return x * M;
}
vec3 crosstalk_inv(vec3 x) {
    float a = CROSSTALK_STRENGTH;
    float b = 1.0 - a;
    float c = 1.0 - 3.0 * a;
    mat3  M = mat3(
    b, -a, -a,
    -a,  b, -a,
    -a, -a,  b) / c;
    return x * M;
}

// 注意输入的颜色不是0～1，而0～MAX_CONTENT_LUMINANCE，大于HDR_REFERENCE_WHITE是高光颜色
vec3 CHROMA_CORRECT(vec3 color) {
    const float L_ref = BT2020_TO_LAB(vec3(HDR_REFERENCE_WHITE)).x;
    const float L_max = BT2020_TO_LAB(vec3(MAX_CONTENT_LUMINANCE)).x;
    color = crosstalk(color);
    color = BT2020_TO_LAB(color);
    color = LAB_to_LCH(color);
    color.y  *= chroma_correction(color.x, L_ref, L_max);
    color = LCH_to_LAB(color);
    color = LAB_TO_BT2020(color);
    color = crosstalk_inv(color);
    return color;
}

const vec3 luma_coeff = vec3(0.262700, 0.677998, 0.059302); // luma weights for BT.2020
const float gcr = luma_coeff.r / luma_coeff.g;
const float gcb = luma_coeff.b / luma_coeff.g;

/* BT.2446-1-2021 method A */
vec3 TONE_MAP(vec3 color)
{
    color = RESCALE_NORMALIZE(color);//输入需要归一化
    const float p_hdr = 1.0 + 32.0 * pow(MAX_CONTENT_LUMINANCE / PQ_MAX_LUMINANCE, 1.0 / 2.4);
    const float p_sdr = 1.0 + 32.0 * pow(HDR_REFERENCE_WHITE / PQ_MAX_LUMINANCE, 1.0 / 2.4);
    vec3 xp = pow(x, vec3(1.0 / 2.4));
    float y_hdr = dot(luma_coeff, xp);

    /* Step 1: convert signal to perceptually linear space */
    float yp = log(1.0 + (p_hdr - 1.0) * y_hdr) / log(p_hdr);

    /* Step 2: apply knee function in perceptual domain */
    float yc = mix(
    1.077 * yp,
    mix((-1.1510 * yp + 2.7811) * yp - 0.6302, 0.5 * yp + 0.5, yp > 0.9909 ? 1.0:0.0),
    yp > 0.7399? 1.0:0.0);

    /* Step 3: convert back to gamma domain */
    float y_sdr = (pow(p_sdr, yc) - 1.0) / (p_sdr - 1.0);

    /* Colour correction */
    float scale = y_sdr / (1.1 * y_hdr);
    float cb_tmo = scale * (xp.b - y_hdr);
    float cr_tmo = scale * (xp.r - y_hdr);
    float y_tmo = y_sdr - max(0.1 * cr_tmo, 0.0);

    /* Convert from Y'Cb'Cr' to R'G'B' (still in BT.2020) */
    float cg_tmo = -(gcr * cr_tmo + gcb * cb_tmo);
    color = y_tmo + vec3(cr_tmo, cg_tmo, cb_tmo);
    color = color*MAX_DISPLAY_LUMINANCE;//输入的范围要0-MAX_DISPLAY_LUMINANCE
    return color;
}
#define cyan_limit 1.518705262732682
#define magenta_limit 1.0750082200767368
#define yellow_limit 1.0887800398456782

#define cyan_threshold 1.0505085424784364
#define magenta_threshold 0.9405097727736265
#define yellow_threshold 0.9771607745933959

// Parabolic compression function: https://www.desmos.com/calculator/nvhp63hmtj
float parabolic(float dist, float lim, float thr) {
    if (dist > thr) {
        // Calculate scale so compression function passes through distance limit: (x=dl, y=1)
        float scale = (1.0 - thr) / sqrt(lim - 1.0);
        float sacle_ = scale * scale / 4.0;
        dist = scale * (sqrt(dist - thr + sacle_) - sqrt(sacle_)) + thr;
    }
    return dist;
}


vec3 GAMUT_MAP(vec3 color) {

    vec3 rgb = BT2020_TO_BT709(color);
    vec3 dl = vec3(cyan_limit, magenta_limit, yellow_limit);

    // Amount of outer gamut to affect
    vec3 th = vec3(cyan_threshold, magenta_threshold, yellow_threshold);

    // Achromatic axis
    float ac = max(max(rgb.r, rgb.g), rgb.b);

    // Inverse RGB Ratios: distance from achromatic axis
    vec3 d = ac == 0.0 ? vec3(0.0) : (ac - rgb) / abs(ac);

    // Compressed distance
    vec3 cd = vec3(
    parabolic(d.x, dl.x, th.x),
    parabolic(d.y, dl.y, th.y),
    parabolic(d.z, dl.z, th.z)
    );

    // Inverse RGB Ratios to RGB
    color = ac - cd * abs(ac);

    return color;
}

void main()
{
    vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
    vec3 rgb = textureColor.rgb;
    vec3 linearColor;
    if(VIDEO_COLOR_SPACE == COLOR_SPACE_BT2020_HLG){
        linearColor = HLG_EOTF(rgb);
    }else if(VIDEO_COLOR_SPACE == COLOR_SPACE_BT2020_PQ){
        linearColor = PQ_EOTF(rgb);
    }else{
        gl_FragColor = vec4(1.0,0.0,0.0,1.0);
        return;
    }
    vec3 absoluteColor = RESCALE_ABSOLUTE(linearColor);
    vec3 chromaCorrectColor = CHROMA_CORRECT(absoluteColor);
    vec3 toneMapColor = TONE_MAP(chromaCorrectColor);
    vec3 normalizeColor = NORMALIZE_DISPLAY(toneMapColor);
    vec3 gamutMapColor = GAMUT_MAP(normalizeColor);
    vec3 finalColor = BT709_OETF(gamutMapColor);
    gl_FragColor.rgb = finalColor;
    gl_FragColor.a = textureColor.a;
}