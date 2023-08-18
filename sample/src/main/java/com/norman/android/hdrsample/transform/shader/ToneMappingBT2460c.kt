package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object ToneMappingBT2460c : GLShaderCode() {
    // ITU-R BT.2446 Conversion Method C
    // https://www.itu.int/pub/R-REP-BT.2446
    override val code: String
        get() = """
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

          vec3 XYZ_to_xyY(vec3 XYZ) {
              float X = XYZ.x;
              float Y = XYZ.y;
              float Z = XYZ.z;

              float divisor = X + Y + Z;
              if (divisor == 0.0) divisor = 1e-6;

              float x = X / divisor;
              float y = Y / divisor;

              return vec3(x, y, Y);
          }

          vec3 xyY_to_XYZ(vec3 xyY) {
              float x = xyY.x;
              float y = xyY.y;
              float Y = xyY.z;

              float multiplo = Y / max(y, 1e-6);

              float z = 1.0 - x - y;
              float X = x * multiplo;
              float Z = z * multiplo;

              return vec3(X, Y, Z);
          }

          const float ip = 0.58535;   // linear length
          const float k1 = 0.83802;   // linear strength
          const float k3 = 0.74204;   // shoulder strength

          float f(float Y, float k1, float k3, float ip) {
              ip /= k1;
              float k2 = (k1 * ip) * (1.0 - k3);
              float k4 = (k1 * ip) - (k2 * log(1.0 - k3));
              return Y < ip ?
                  Y * k1 :
                  log((Y / ip) - k3) * k2 + k4;
          }

          float curve(float x) {
              const float over_white = 1019.0 / 940.0;    // 109% range (super-whites)
              return f(x, k1, k3, ip) / over_white;
          }

          vec4 ${javaClass.name}(vec4 color) {
              color.rgb = RGB_to_XYZ(color.rgb);
              color.rgb = XYZ_to_xyY(color.rgb);
              color.z   = curve(color.z);
              color.rgb = xyY_to_XYZ(color.rgb);
              color.rgb = XYZ_to_RGB(color.rgb);
              return color;
          }
        """.trimIndent()
}