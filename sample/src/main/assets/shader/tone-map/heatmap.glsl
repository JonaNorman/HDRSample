// Heatmap

//!PARAM enabled
//!TYPE int
//!MINIMUM 0
//!MAXIMUM 5
1

//!PARAM L_sdr
//!TYPE float
//!MINIMUM 0
//!MAXIMUM 1000
203.0

//!PARAM CONTRAST_sdr
//!TYPE float
//!MINIMUM 0
//!MAXIMUM 1000000
1000.0

//!HOOK OUTPUT
//!BIND HOOKED
//!WHEN enabled
//!DESC tone mapping (heatmap)

vec4 color = HOOKED_tex(HOOKED_pos);
vec4 hook() {
    const float l0 =     1.0 / CONTRAST_sdr;
    const float l1 =     1.0;
    const float l2 =  1000.0 / L_sdr;
    const float l3 =  2000.0 / L_sdr;
    const float l4 =  4000.0 / L_sdr;
    const float l5 = 10000.0 / L_sdr;

    float L = 0.0;
    if (enabled == 1) {
        // Y (relative luminance)
        L = dot(color.rgb, vec3(0.2627, 0.6780, 0.0593));
    } else if (enabled == 2) {
        // maxRGB
        L = max(max(color.r, color.g), color.b);
    } else if (enabled == 3) {
        // Mean (arithmetic)
        L = (color.r + color.g + color.b) / 3.0;
    } else if (enabled == 4) {
        // Mean (geometric)
        L = pow((color.r * color.g * color.b), 1.0 / 3.0);
    }

    if (L > l5) {
        color.rgb = vec3(1.0, 0.0, 0.6);
    } else if (L > l4) {
        float a = (L - l4) / (l5 - l4);
        a = max(a - 0.1, 0.0) / 0.9 + 0.1;
        color.rgb = vec3(1.0, 1.0, a);
    } else if (L > l3) {
        float a = (L - l3) / (l4 - l3);
        a = max(a - 0.1, 0.0) / 0.9 + 0.1;
        color.rgb = vec3(a, 0.0, 0.0);
    } else if (L > l2) {
        float a = (L - l2) / (l3 - l2);
        a = max(a - 0.1, 0.0) / 0.9 + 0.1;
        color.rgb = vec3(0.0, a, 0.0);
    } else if (L > l1) {
        float a = (L - l1) / (l2 - l1);
        a = max(a - 0.1, 0.0) / 0.9 + 0.1;
        color.rgb = vec3(0.0, 0.0, a);
    } else if (L < l0) {
        color.rgb = vec3(0.0, 0.0, 0.0);
    } else {
        color.rgb = vec3(L, L, L);
    }

    return color;
}
