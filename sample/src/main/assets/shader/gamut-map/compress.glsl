// Compress highly chromatic source colorimetry into a smaller gamut
// https://github.com/jedypod/gamut-compress
// https://github.com/ampas/aces-dev/blob/dev/transforms/ctl/lmt/LMT.Academy.ReferenceGamutCompress.ctl

//!PARAM cyan_limit
//!TYPE float
//!MINIMUM 1.001
//!MAXIMUM 2
1.6515689028157825

//!PARAM magenta_limit
//!TYPE float
//!MINIMUM 1.001
//!MAXIMUM 2
1.7355376392652817

//!PARAM yellow_limit
//!TYPE float
//!MINIMUM 1.001
//!MAXIMUM 2
1.671460554049985

//!PARAM cyan_threshold
//!TYPE float
//!MINIMUM 0
//!MAXIMUM 1
0.4770771960073855

//!PARAM magenta_threshold
//!TYPE float
//!MINIMUM 0
//!MAXIMUM 1
0.43305325213057494

//!PARAM yellow_threshold
//!TYPE float
//!MINIMUM 0
//!MAXIMUM 1
0.8430686842640781

//!PARAM select
//!TYPE float
//!MINIMUM 0
//!MAXIMUM 1
0.1

//!HOOK OUTPUT
//!BIND HOOKED
//!DESC gamut mapping (compress)

mat3 M = mat3(
     1.6605, -0.5876, -0.0728,
    -0.1246,  1.1329, -0.0083,
    -0.0182, -0.1006,  1.1187);

vec4 color = HOOKED_tex(HOOKED_pos);
vec4 hook() {
    vec3 color_src = color.rgb;
    vec3 color_src_cliped = clamp(color_src, 0.0, 1.0);
    vec3 color_dst = color_src_cliped * M;

    vec3 rgb = color_dst;

    // Distance limit: How far beyond the gamut boundary to compress
    vec3 dl = vec3(cyan_limit, magenta_limit, yellow_limit);

    // Amount of outer gamut to affect
    vec3 th = vec3(cyan_threshold, magenta_threshold, yellow_threshold);

    // Achromatic axis
    float ac = max(rgb.x, max(rgb.y, rgb.z));

    // Inverse RGB Ratios: distance from achromatic axis
    vec3 d = ac == 0.0 ? vec3(0.0) : (ac - rgb) / abs(ac);

    // Calculate scale so compression function passes through distance limit: (x=dl, y=1)
    vec3 s;
    s.x = (1.0 - th.x) / sqrt(dl.x - 1.0);
    s.y = (1.0 - th.y) / sqrt(dl.y - 1.0);
    s.z = (1.0 - th.z) / sqrt(dl.z - 1.0);

    vec3 cd; // Compressed distance
    // Parabolic compression function: https://www.desmos.com/calculator/nvhp63hmtj
    cd.x = d.x < th.x ? d.x : s.x * sqrt(d.x - th.x + s.x * s.x / 4.0) - s.x * sqrt(s.x * s.x / 4.0) + th.x;
    cd.y = d.y < th.y ? d.y : s.y * sqrt(d.y - th.y + s.y * s.y / 4.0) - s.y * sqrt(s.y * s.y / 4.0) + th.y;
    cd.z = d.z < th.z ? d.z : s.z * sqrt(d.z - th.z + s.z * s.z / 4.0) - s.z * sqrt(s.z * s.z / 4.0) + th.z;

    // Inverse RGB Ratios to RGB
    vec3 crgb = ac - cd * abs(ac);

    color.rgb = mix(rgb, crgb, select);

    return color;
}
