package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object ToneMappingJzazbz : GLShaderCode() {

    /*
        https://github.com/natural-harmonia-gropius/hdr-toys/blob/master/_dev/tone-mapping/linear_jzazbz.glsl

     */
    override val code: String
        get() = """
            #define L_hdr 1000.0
            #define L_sdr 203.0
            const float pq_m1 = 0.1593017578125;
            const float pq_m2 = 78.84375;
            const float pq_c1 = 0.8359375;
            const float pq_c2 = 18.8515625;
            const float pq_c3 = 18.6875;

            const float pq_C  = 10000.0;

            float Y_to_ST2084(float C) {
                float L = C / pq_C;
                float Lm = pow(L, pq_m1);
                float N = (pq_c1 + pq_c2 * Lm) / (1.0 + pq_c3 * Lm);
                N = pow(N, pq_m2);
                return N;
            }

            float ST2084_to_Y(float N) {
                float Np = pow(N, 1.0 / pq_m2);
                float L = Np - pq_c1;
                if (L < 0.0 ) L = 0.0;
                L = L / (pq_c2 - pq_c3 * Np);
                L = pow(L, 1.0 / pq_m1);
                return L * pq_C;
            }

            vec3 RGB_to_XYZ(vec3 RGB) {
                mat3 M = mat3(
                    0.6369580483012914, 0.14461690358620832,  0.1688809751641721,
                    0.2627002120112671, 0.6779980715188708,   0.05930171646986196,
                    0.000000000000000,  0.028072693049087428, 1.060985057710791);
                return RGB * M;
            }

            vec3 XYZ_to_RGB(vec3 XYZ) {
                mat3 M = mat3(
                     1.716651187971268,  -0.355670783776392, -0.253366281373660,
                    -0.666684351832489,   1.616481236634939,  0.0157685458139111,
                     0.017639857445311,  -0.042770613257809,  0.942103121235474);
                return XYZ * M;
            }

            vec3 XYZ_to_Cone(vec3 XYZ) {
                mat3 M = mat3(
                     0.41478972, 0.579999,  0.0146480,
                    -0.2015100,  1.120649,  0.0531008,
                    -0.0166008,  0.264800,  0.6684799);
                return XYZ * M;
            }

            vec3 Cone_to_XYZ(vec3 LMS) {
                mat3 M = mat3(
                     1.9242264357876067,  -1.0047923125953657,  0.037651404030618,
                     0.35031676209499907,  0.7264811939316552, -0.06538442294808501,
                    -0.09098281098284752, -0.3127282905230739,  1.5227665613052603);
                return LMS * M;
            }

            vec3 Cone_to_Iab(vec3 LMS) {
                mat3 M = mat3(
                    0.5,       0.5,       0.0,
                    3.524000, -4.066708,  0.542708,
                    0.199076,  1.096799, -1.295875);
                return LMS * M;
            }

            vec3 Iab_to_Cone(vec3 Iab) {
                mat3 M = mat3(
                    1.0,                 0.1386050432715393,   0.05804731615611886,
                    0.9999999999999999, -0.1386050432715393,  -0.05804731615611886,
                    0.9999999999999998, -0.09601924202631895, -0.8118918960560388);
                return Iab * M;
            }

            const float b = 1.15;
            const float g = 0.66;

            const float d = -0.56;
            const float d0 = 1.6295499532821566e-11;

            vec3 RGB_to_Jzazbz(vec3 color) {
                color *= L_sdr;

                color = RGB_to_XYZ(color);

                float Xm = (b * color.x) - ((b - 1.0) * color.z);
                float Ym = (g * color.y) - ((g - 1.0) * color.x);

                color = XYZ_to_Cone(vec3(Xm, Ym, color.z));

                color.r = Y_to_ST2084(color.r);
                color.g = Y_to_ST2084(color.g);
                color.b = Y_to_ST2084(color.b);

                color = Cone_to_Iab(color);

                color.r = ((1.0 + d) * color.r) / (1.0 + (d * color.r)) - d0;

                return color;
            }

            vec3 Jzazbz_to_RGB(vec3 color) {
                color.r = (color.r + d0) / (1.0 + d - d * (color.r + d0));

                color = Iab_to_Cone(color);

                color.r = ST2084_to_Y(color.r);
                color.g = ST2084_to_Y(color.g);
                color.b = ST2084_to_Y(color.b);

                color = Cone_to_XYZ(color);

                float Xa = (color.x + ((b - 1.0) * color.z)) / b;
                float Ya = (color.y + ((g - 1.0) * Xa)) / g;

                color = XYZ_to_RGB(vec3(Xa, Ya, color.z));

                color /= L_sdr;

                return color;
            }

            float curve(float x) {
                const float iw = RGB_to_Jzazbz(vec3(L_hdr / L_sdr)).x;
                const float ow = RGB_to_Jzazbz(vec3(1.0)).x;
                const float w = iw / ow;
                return x / w;
            }

            vec3 tone_mapping_ictcp(vec3 ICtCp) {
                float I2  = curve(ICtCp.x);
                ICtCp.yz *= min(ICtCp.x / I2, I2 / ICtCp.x);
                ICtCp.x   = I2;

                return ICtCp;
            }

            vec4 ${javaClass.name}(vec4 color) {
                color.rgb = RGB_to_Jzazbz(color.rgb);
                color.rgb = tone_mapping_ictcp(color.rgb);
                color.rgb = Jzazbz_to_RGB(color.rgb);

                return color;
            }
        """.trimIndent()
}