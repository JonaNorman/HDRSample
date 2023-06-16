// Simple conversion from BT.2020 to BT.709 based on linear matrix transformation

//!HOOK OUTPUT
//!BIND HOOKED
//!DESC gamut mapping (matrix)

mat3 M = mat3(
     1.6605, -0.5876, -0.0728,
    -0.1246,  1.1329, -0.0083,
    -0.0182, -0.1006,  1.1187);

vec4 color = HOOKED_tex(HOOKED_pos);
vec4 hook() {
    vec3 color_src = color.rgb;
    vec3 color_src_cliped = clamp(color_src, 0.0, 1.0);
    vec3 color_dst = color_src_cliped * M;
    vec3 color_dst_cliped = clamp(color_dst, 0.0, 1.0);

    color.rgb = color_dst_cliped;

    return color;
}
