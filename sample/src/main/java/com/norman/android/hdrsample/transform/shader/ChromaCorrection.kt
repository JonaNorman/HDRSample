package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object ChromaCorrection : GLShaderCode() {
    //色度矫正，该方法是BT2446C方法(https://www.itu.int/dms_pub/itu-r/opb/rep/R-REP-BT.2446-2019-PDF-E.pdf)中介绍，参考https://github.com/natural-harmonia-gropius/hdr-toys/blob/master/utils/chroma_correction.glsl
    //我的理解是在色调映射前面对超过HDR参考白的颜色进行消色处理使SDR内容的高亮部分看起来像白光
    /**
     * 步骤1: crosstalk矩阵解决后续RGB颜色计算的干扰现象，个人觉得是经验值，alpha从0-0.5
     * 步骤2: RGB转换成LAB(L亮度A绿色到红色分量B蓝色到黄色分量)，LAB再转换LCH(L亮度C色度H色调)
     * 步骤3: 对HDR参考白以上的颜色LCH的C通道(色度)进行处理，亮度越高，色度越低
     * 步骤4: 还原成RGB颜色
     */

    override val code: String
        get() = """
        #define L_hdr 1000.0//HDR亮度最大值
        #define L_sdr 203.0//HDR参考白亮度
        #define sigma 0.05//色度调整程度
        #define alpha 0.04//颜色串扰crosstalk程度
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

       vec3 XYZ_ref = RGB_to_XYZ(vec3(L_sdr));

       vec3 XYZ_to_Lab(vec3 XYZ) {
           float X = XYZ.x;
           float Y = XYZ.y;
           float Z = XYZ.z;

           X = f1(X / XYZ_ref.x, delta);
           Y = f1(Y / XYZ_ref.y, delta);
           Z = f1(Z / XYZ_ref.z, delta);

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

           X = f2(X, delta) * XYZ_ref.x;
           Y = f2(Y, delta) * XYZ_ref.y;
           Z = f2(Z, delta) * XYZ_ref.z;

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

       const float pi = 3.141592653589793;
       const float epsilon = 1e-6;

       vec3 Lab_to_LCH(vec3 Lab) {
           float a = Lab.y;
           float b = Lab.z;

           float C = length(vec2(a, b));
           float H = 0.0;

           if (!(abs(a) < epsilon && abs(b) < epsilon)) {
               H = atan(b, a);
               H = H * 180.0 / pi;
               H = mod((mod(H, 360.0) + 360.0), 360.0);
           }

           return vec3(Lab.x, C, H);
       }

       vec3 LCH_to_Lab(vec3 LCH) {
           float C = max(LCH.y, 0.0);
           float H = LCH.z * pi / 180.0;

           float a = C * cos(H);
           float b = C * sin(H);

           return vec3(LCH.x, a, b);
       }

       float chroma_correction(float L, float Lref, float Lmax, float sigma) {
           if (L > Lref) {
               return max(1.0 - sigma * (L - Lref) / (Lmax - Lref), 0.0);
           }

           return 1.0;
       }

       vec3 crosstalk(vec3 x, float a) {
           float b = 1.0 - 2.0 * a;
           mat3  M = mat3(
               b, a, a,
               a, b, a,
               a, a, b);
           return x * M;
       }

       vec3 crosstalk_inv(vec3 x, float a) {
           float b = 1.0 - a;
           float c = 1.0 - 3.0 * a;
           mat3  M = mat3(
                b, -a, -a,
               -a,  b, -a,
               -a, -a,  b) / c;
           return x * M;
       }

       vec4 ${javaClass.name}(vec4 color) {
           const float L_ref = RGB_to_Lab(vec3(1.0)).x;
           const float L_max = RGB_to_Lab(vec3(L_hdr / L_sdr)).x;
           color.rgb = crosstalk(color.rgb, alpha);
           color.rgb = RGB_to_Lab(color.rgb);
           color.rgb = Lab_to_LCH(color.rgb);
           color.y  *= chroma_correction(color.x, L_ref, L_max, sigma);
           color.rgb = LCH_to_Lab(color.rgb);
           color.rgb = Lab_to_RGB(color.rgb);
           color.rgb = crosstalk_inv(color.rgb, alpha);
           return color;
       }
        """.trimIndent()
}