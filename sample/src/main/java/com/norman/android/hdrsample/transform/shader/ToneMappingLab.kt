package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object ToneMappingLab : GLShaderCode() {

    /*
        https://github.com/natural-harmonia-gropius/hdr-toys/blob/master/_dev/tone-mapping/linear_lab.glsl
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

            vec3 XYZD65_to_XYZD50(vec3 XYZ) {
                mat3 M = mat3(
                     1.0479298208405488,   0.022946793341019088, -0.05019222954313557,
                     0.029627815688159344, 0.990434484573249,    -0.01707382502938514,
                    -0.009243058152591178, 0.015055144896577895,  0.7518742899580008);
                return XYZ * M;
            }

            vec3 XYZD50_to_XYZD65(vec3 XYZ) {
                mat3 M = mat3(
                     0.9554734527042182,   -0.023098536874261423, 0.0632593086610217,
                    -0.028369706963208136,  1.0099954580058226,   0.021041398966943008,
                     0.012314001688319899, -0.020507696433477912, 1.3303659366080753);
                return XYZ * M;
            }

            float delta = 6.0 / 29.0;
            float deltac = delta * 2.0 / 3.0;

            float f1(float x, float delta) {
                return x > pow(delta, 3.0) ?
                    cbrt(x) :
                    deltac + x / (3.0 * pow(delta, 2.0));
            }

            float f2(float x, float delta) {
                return x > delta ?
                    pow(x, 3.0) :
                    (x - deltac) * (3.0 * pow(delta, 2.0));
            }

            vec3 XYZn = RGB_to_XYZ(vec3(L_sdr));

            vec3 XYZ_to_Lab(vec3 XYZ) {
                float X = XYZ.x;
                float Y = XYZ.y;
                float Z = XYZ.z;

                X = f1(X / XYZn.x, delta);
                Y = f1(Y / XYZn.y, delta);
                Z = f1(Z / XYZn.z, delta);

                float L = 116.0 * Y - 16.0;
                float a = 500.0 * (X - Y);
                float b = 200.0 * (Y - Z);

                return vec3(L, a, b);
            }

            vec3 Lab_to_XYZ(vec3 Lab) {
                float L = Lab.x;
                float a = Lab.y;
                float b = Lab.z;

                float Y = (L + 16.0) / 116.0;
                float X = Y + a / 500.0;
                float Z = Y - b / 200.0;

                X = f2(X, delta) * XYZn.x;
                Y = f2(Y, delta) * XYZn.y;
                Z = f2(Z, delta) * XYZn.z;

                return vec3(X, Y, Z);
            }

            vec3 RGB_to_Lab(vec3 color) {
                color *= L_sdr;
                color  = RGB_to_XYZ(color);
                color  = XYZD65_to_XYZD50(color);
                color  = XYZ_to_Lab(color);
                return color;
            }

            vec3 Lab_to_RGB(vec3 color) {
                color  = Lab_to_XYZ(color);
                color  = XYZD50_to_XYZD65(color);
                color  = XYZ_to_RGB(color);
                color /= L_sdr;
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