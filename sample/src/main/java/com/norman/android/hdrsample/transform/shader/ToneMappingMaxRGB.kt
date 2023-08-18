package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object ToneMappingMaxRGB : GLShaderCode() {

    /*
        https://github.com/natural-harmonia-gropius/hdr-toys/blob/master/_dev/tone-mapping/linear_oklab.glsl
     */
    override val code: String
        get() = """
            #define L_hdr 1000.0
            #define L_sdr 203.0

            #define cbrt(x) (sign(x) * pow(abs(x), 1.0 / 3.0))


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
                    0.8190224432164319,   0.3619062562801221,  -0.12887378261216414,
                    0.0329836671980271,   0.9292868468965546,   0.03614466816999844,
                    0.048177199566046255, 0.26423952494422764,  0.6335478258136937);
                return XYZ * M;
            }

            vec3 LMS_to_XYZ(vec3 LMS) {
                mat3 M = mat3(
                     1.2268798733741557,  -0.5578149965554813,  0.28139105017721583,
                    -0.04057576262431372,  1.1122868293970594, -0.07171106666151701,
                    -0.07637294974672142, -0.4214933239627914,  1.5869240244272418);
                return LMS * M;
            }

            vec3 LMS_to_Lab(vec3 LMS) {
                mat3 M = mat3(
                    0.2104542553,  0.7936177850, -0.0040720468,
                    1.9779984951, -2.4285922050,  0.4505937099,
                    0.0259040371,  0.7827717662, -0.8086757660);

                LMS = vec3(
                    cbrt(LMS.x),
                    cbrt(LMS.y),
                    cbrt(LMS.z)
                );

                return LMS * M;
            }

            vec3 Lab_to_LMS(vec3 Lab) {
                mat3 M = mat3(
                    0.99999999845051981432,  0.39633779217376785678,   0.21580375806075880339,
                    1.0000000088817607767,  -0.1055613423236563494,   -0.063854174771705903402,
                    1.0000000546724109177,  -0.089484182094965759684, -1.2914855378640917399);

                Lab = Lab * M;

                return vec3(
                    pow(Lab.x, 3.0),
                    pow(Lab.y, 3.0),
                    pow(Lab.z, 3.0)
                );
            }

            vec3 RGB_to_Lab(vec3 color) {
                color  = RGB_to_XYZ(color);
                color  = XYZ_to_LMS(color);
                color  = LMS_to_Lab(color);
                return color;
            }

            vec3 Lab_to_RGB(vec3 color) {
                color  = Lab_to_LMS(color);
                color  = LMS_to_XYZ(color);
                color  = XYZ_to_RGB(color);
                return color;
            }

            float curve(float x) {
                const float iw = RGB_to_Lab(vec3(L_hdr / L_sdr)).x;
                const float ow = RGB_to_Lab(vec3(1.0)).x;
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

                color.rgb = RGB_to_Lab(color.rgb);
                color.rgb = tone_mapping_ictcp(color.rgb);
                color.rgb = Lab_to_RGB(color.rgb);

                return color;
            }
        """.trimIndent()
}