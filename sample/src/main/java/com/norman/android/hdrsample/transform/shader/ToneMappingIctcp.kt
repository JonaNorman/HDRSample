package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object ToneMappingIctcp : GLShaderCode() {

    /*
        https://github.com/natural-harmonia-gropius/hdr-toys/blob/master/_dev/tone-mapping/linear_ictcp.glsl

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

            vec3 XYZ_to_LMS(vec3 XYZ) {
                mat3 M = mat3(
                     0.3592, 0.6976, -0.0358,
                    -0.1922, 1.1004,  0.0755,
                     0.0070, 0.0749,  0.8434);
                return XYZ * M;
            }

            vec3 LMS_to_XYZ(vec3 LMS) {
                mat3 M = mat3(
                     2.070180056695613509600, -1.326456876103021025500,  0.206616006847855170810,
                     0.364988250032657479740,  0.680467362852235141020, -0.045421753075853231409,
                    -0.049595542238932107896, -0.049421161186757487412,  1.187995941732803439400);
                return LMS * M;
            }

            vec3 LMS_to_ICtCp(vec3 LMS) {
                LMS.x = Y_to_ST2084(LMS.x);
                LMS.y = Y_to_ST2084(LMS.y);
                LMS.z = Y_to_ST2084(LMS.z);
                mat3 M = mat3(
                     2048,   2048,    0,
                     6610, -13613, 7003,
                    17933, -17390, -543) / 4096;
                return LMS * M;
            }

            vec3 ICtCp_to_LMS(vec3 ICtCp) {
                mat3 M = mat3(
                    0.99998889656284013833,  0.00860505014728705821,  0.11103437159861647860,
                    1.00001110343715986160, -0.00860505014728705821, -0.11103437159861647860,
                    1.00003206339100541200,  0.56004913547279000113, -0.32063391005412026469);
                ICtCp *= M;
                ICtCp.x = ST2084_to_Y(ICtCp.x);
                ICtCp.y = ST2084_to_Y(ICtCp.y);
                ICtCp.z = ST2084_to_Y(ICtCp.z);
                return ICtCp;
            }

            vec3 RGB_to_ICtCp(vec3 color) {
                color *= L_sdr;
                color = RGB_to_XYZ(color);
                color = XYZ_to_LMS(color);
                color = LMS_to_ICtCp(color);
                return color;
            }

            vec3 ICtCp_to_RGB(vec3 color) {
                color = ICtCp_to_LMS(color);
                color = LMS_to_XYZ(color);
                color = XYZ_to_RGB(color);
                color /= L_sdr;
                return color;
            }

            float curve(float x) {
                const float iw = Y_to_ST2084(L_hdr);
                const float ow = Y_to_ST2084(L_sdr);
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
                color.rgb = RGB_to_ICtCp(color.rgb);
                color.rgb = tone_mapping_ictcp(color.rgb);
                color.rgb = ICtCp_to_RGB(color.rgb);

                return color;
            }
        """.trimIndent()
}