package com.norman.android.hdrsample.transform.shader.tonemap

import com.norman.android.hdrsample.transform.shader.ColorConversion
import com.norman.android.hdrsample.transform.shader.ReScale

/**
 * 该实现是BT2446中的C方法
 * 把RGB转成xyY，然后把xyY中的Y用f公式进行调整，最后再转换回来
 *
 * f公式个人理解是这样的
 * 其实就是两个点进行插值
 * 1. (ip,YHDRip)表示SDR拐点,拐点前面的值按k1斜率插值
 *  ip表示SDR部分的最大亮度，由BT2408中写的HDR和SDR肤色的关系决定的也就是SDR的80%换算成100cd/m2也就是58.535cd/m2
 *  YHDRip是由斜率k1和ip的值算出来的，YHDRip = ip/k1=58.535/k1
 * 2.  (HDR参考白,YSDRwp)表示SDR和HDR的高光拐点，按ln进行插值
 *
 * 参考：
 * https://www.itu.int/pub/R-REP-BT.2446
 * https://github.com/natural-harmonia-gropius/hdr-toys/blob/master/tone-mapping/bt2446c.glsl
 */

class ToneMapBT2446C : ToneMap() {
    override val code: String
        get() = """
          const float ip = 58.535;   // linear length 输入是绝对值，把hdr-toys中的0.58535乘100，
          const float k1 = 0.83802;   // linear strength
          const float k3 = 0.74204;   // shoulder strength

          float f(float Y, float k1, float k3, float ip) {
              ip /= k1;
              float k2 = (k1 * ip) * (1.0 - k3);//曲线两部分斜率相等
              float k4 = (k1 * ip) - (k2 * log(1.0 - k3));//曲线交点值相等
              return Y < ip ?
                  Y * k1 :
                  log((Y / ip) - k3) * k2 + k4;
          }

          float curve(float x) {//  hdr-toys中还乘以over_white =1019.0 / 940.0，这里决定不乘，因为前面处理过已经是全范围了
              return f(x, k1, k3, ip);
          }

          vec3 $methodToneMap(vec3 color) {
              color = ${ReScale.methodScaleReferenceWhiteToOne}(color);
              color= ${ColorConversion.methodBt2020ToXYZ}(color);
              color = ${ColorConversion.methodXYZToxyY}(color);
              color.z   = curve(color.z);
              color = ${ColorConversion.methodxyYToXYZ}(color);
              color = ${ColorConversion.methodXYZToBt2020}(color);
              return color;
          }
        """.trimIndent()
}