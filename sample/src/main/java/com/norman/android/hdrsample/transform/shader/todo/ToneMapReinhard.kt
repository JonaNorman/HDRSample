package com.norman.android.hdrsample.transform.shader.todo

import com.norman.android.hdrsample.opengl.GLShaderCode

object ToneMapReinhard : GLShaderCode() {

    // 通过ReinhardCurve曲线作为缩放值改变颜色
    // Extended mapping by Reinhard et al. 2002. which allows high luminances to burn out.
    // https://www.researchgate.net/publication/2908938_Photographic_Tone_Reproduction_For_Digital_Images
    override val code: String
        get() = """
          #define L_hdr 1000.0
          #define L_sdr 203.0

          float curve(float x) {
              const float w = L_hdr / L_sdr;
              const float simple = x / (1.0 + x);
              const float extended = simple * (1.0 + x / (w * w));
              return extended;
          }

          vec3 tone_mapping_y(vec3 RGB) {
              const float y = dot(RGB, vec3(0.2627002120112671, 0.6779980715188708, 0.05930171646986196));
              return RGB * curve(y) / y;
          }

          vec4 ${javaClass.name}(vec4 color) {
              color.rgb = tone_mapping_y(color.rgb);
              return color;
          }
        """.trimIndent()
}