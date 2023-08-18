package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object GamutMapCompress :GLShaderCode() {

    /**
     * 压缩BT2020色域到BT709时会产生一些超过边界的值，个人理解相当于有两个色域三角形，一个是BT709threshold颜色三角形，一个BT2020limit颜色三角形，
     * 两个三角形之间就是通过矩阵转换后不正常的颜色，通过Parabolic compression function数学公式压缩不正常的颜色
     * 参考至https://github.com/natural-harmonia-gropius/hdr-toys/blob/master/gamut-mapping/jedypod.glsl
     * https://github.com/jedypod/gamut-compress
     * https://github.com/ampas/aces-dev/blob/dev/transforms/ctl/lmt/LMT.Academy.ReferenceGamutCompress.ctl
     *
     */

    override val code: String
        get() = """
            #define cyan_limit 1.518705262732682
            #define magenta_limit 1.0750082200767368
            #define yellow_limit 1.0887800398456782
            
            #define cyan_threshold 1.0505085424784364
            #define magenta_threshold 0.9405097727736265
            #define yellow_threshold 0.9771607745933959
            

            vec4 ${javaClass.name}() {
                vec3 color_src = color.rgb;
                vec3 color_dst = BT2020_TO_BT709(color_src);
                vec3 rgb = gamut_compress(color_dst);

               // Distance limit: How far beyond the gamut boundary to compress
                vec3 dl = vec3(cyan_limit, magenta_limit, yellow_limit);

                // Amount of outer gamut to affect
                vec3 th = vec3(cyan_threshold, magenta_threshold, yellow_threshold);

                // Achromatic axis
                float ac = max(max(rgb.r, rgb.g), rgb.b);

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
                return crgb;
            }
        """.trimIndent()
}