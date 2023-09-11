package com.norman.android.hdrsample.transform.shader.gamutmap

import com.norman.android.hdrsample.transform.shader.ColorConversion.methodBt2020ToBt709

/**
 * 色域映射的压缩处理方式
 * BT2020色域到BT709时会产生超过边界的值，有两个色域三角形，一个是BT709颜色三角形，一个BT2020颜色三角形
 * 两个三角形之间的颜色是通过矩阵转换后不正常的颜色，对高光颜色进行消色处理(可以理解成降低饱和度)
 * 消色处理是通过对消色轴的距离进行Parabolic数学公式缩放进行的
 * 参考至
 * https://github.com/natural-harmonia-gropius/hdr-toys/blob/master/gamut-mapping/jedypod.glsl
 * https://github.com/jedypod/gamut-compress
 * https://github.com/ampas/aces-dev/blob/dev/transforms/ctl/lmt/LMT.Academy.ReferenceGamutCompress.ctl
 *
 */
class GamutMapCompress : GamutMap() {

    override val code: String
        get() = """
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
            

            vec3 $methodGamutMap(vec3 color) {
            
                vec3 rgb = $methodBt2020ToBt709(color);
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
            """.trimIndent()
}