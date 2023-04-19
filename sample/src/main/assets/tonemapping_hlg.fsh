#define  ARIB_B67_A  0.17883277f
#define  ARIB_B67_B  0.28466892f
#define  ARIB_B67_C  0.55991073f

precision highp float;
uniform sampler2D inputImageTexture;
varying highp vec2 textureCoordinate;
uniform float max_screen_luminance;
uniform float max_peak_luminance;




highp float arib_b67_inverse_oetf(highp float x)
{
    x = max(x, 0.0);
    if (x <= (1.0/2.0))
    x = (x * x) * (1.0 / 3.0);
    else
    x = (exp((x - ARIB_B67_C) / ARIB_B67_A) + ARIB_B67_B) / 12.0;
    return x;
}

float hable(float v)
{
    float a = 0.15, b = 0.50, c = 0.10, d = 0.20, e = 0.02, f = 0.30;
    return (v * (v * a + b * c) + d * e) / (v * (v * a + b) + d * f) - e / f;
}


highp float arib_b67_ootf(highp float x)
{
    return x < 0.0 ? x : pow(x, 1.2);
}
highp float arib_b67_eotf(highp float x)
{
    return arib_b67_ootf(arib_b67_inverse_oetf(x));
}
highp float arib_b67_oetf(highp float x)
{
    x = max(x, 0.0);
    if (x <= (1.0 / 12.0))
    x = sqrt(3.0 * x);
    else
    x = ARIB_B67_A * log(12.0 * x - ARIB_B67_B) + ARIB_B67_C;
    return x;
}

float Lb = 0.1f;
float Lw = 302.0f;
float sys_gamma = 1.001f;
highp float bfiler(highp float x) {
    float b = sqrt(3.0 * pow((Lb/Lw), (1.0/sys_gamma)));
    return max(0.0, ((1.0-b)*x + b));
}

float max3(float a, float  b, float c){
    return max(max(a, b), c);
}

void main() {

    highp vec4 rgb10bit =  texture2D(inputImageTexture, textureCoordinate);
    // 电 转线性光信号
    highp vec3 fragColor = 2.5 * vec3(arib_b67_eotf(bfiler(rgb10bit.r)), arib_b67_eotf(bfiler(rgb10bit.g)), arib_b67_eotf(bfiler(rgb10bit.b)));
    // HDR线性 ToneMapping映射转成 SDR线性
    highp float sig;
    highp float sig_orig;
    sig = max(max3(fragColor.r, fragColor.g, fragColor.b), 1e-6);
    sig_orig = sig;
    float peak = max_peak_luminance / 100.0;//  MaxCLL / REFERENCE_WHITE(100);
    sig = hable(sig) / hable(peak);
    fragColor.r = fragColor.r * (sig / sig_orig);
    fragColor.g = fragColor.g * (sig / sig_orig);
    fragColor.b = fragColor.b * (sig / sig_orig);
    // 逆线性光信号，变回电
    fragColor = vec3(arib_b67_oetf(fragColor.r), arib_b67_oetf(fragColor.g), arib_b67_oetf(fragColor.b));
    gl_FragColor = vec4(fragColor, 1.0);
}





