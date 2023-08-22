package com.norman.android.hdrsample.transform.shader

//https://github.com/natural-harmonia-gropius/hdr-toys/blob/master/tone-mapping/bt2446c.glsl
object ToneMapBT2446c : GamutMap() {
    // ITU-R BT.2446 Conversion Method C
    // https://www.itu.int/pub/R-REP-BT.2446
    override val code: String
        get() = """
         
          

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

          vec3 $methodGamutMap(vec3 color) {
              color.rgb = ${ColorConversion.methodBt2020ToXYZ}(color.rgb);
              color.rgb = ${ColorConversion.methodXYZToxyY}(color.rgb);
              color.z   = curve(color.z);
              color.rgb = ${ColorConversion.methodxyYToXYZ}(color.rgb);
              color.rgb = ${ColorConversion.methodXYZToBt2020}(color.rgb);
              return color;
          }
        """.trimIndent()
}