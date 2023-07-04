// PQ公式参数详解见 https://juejin.cn/post/7231369710024310821#heading-13
#define PQ_M1  0.1593017578125
#define PQ_M2  78.84375
#define PQ_C1  0.8359375
#define PQ_C2  18.8515625
#define PQ_C3  18.6875
#define PQ_MAX_LUMINANCE  10000.0   //PQ最大亮度
#define PQ_REFERENCE_WHITE 203.0 // PQ参考白



vec3 PQ_EOTF(vec3 x)
{
    vec3 p = pow(x, vec3(1.0 / PQ_M2));
    vec3 num = max(p - PQ_C1, 0.0);
    vec3 den = PQ_C2 - PQ_C3 * p;
    vec3 Y = pow(num / den, vec3(1.0 / PQ_M1));
    return  Y;
}

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