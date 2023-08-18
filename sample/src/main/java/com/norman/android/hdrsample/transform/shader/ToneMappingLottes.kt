package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object ToneMappingLottes : GLShaderCode() {
    // Filmic curve by Timothy Lottes, Also known as the "AMD curve".
    // https://www.gdcvault.com/play/1023512/Advanced-Graphics-Techniques-Tutorial-Day
    override val code: String
        get() = """
           #define L_hdr 1000.0
           #define L_sdr 203.0
           // General tonemapping operator, build 'b' term.
           float f1(float hdrMax, float contrast, float shoulder, float midIn, float midOut) {
               return
                   -((-pow(midIn, contrast) + (midOut * (pow(hdrMax, contrast * shoulder) * pow(midIn, contrast) -
                       pow(hdrMax, contrast) * pow(midIn, contrast * shoulder) * midOut)) /
                       (pow(hdrMax, contrast * shoulder) * midOut - pow(midIn, contrast * shoulder) * midOut)) /
                       (pow(midIn, contrast * shoulder) * midOut));
           }

           // General tonemapping operator, build 'c' term.
           float f2(float hdrMax, float contrast, float shoulder, float midIn, float midOut) {
               return (pow(hdrMax, contrast * shoulder) * pow(midIn, contrast) - pow(hdrMax, contrast) * pow(midIn, contrast * shoulder) * midOut) /
                      (pow(hdrMax, contrast * shoulder) * midOut - pow(midIn, contrast * shoulder) * midOut);
           }

           // General tonemapping operator, p := {contrast,shoulder,b,c}.
           float f3(float x, vec4 p) {
               float z = pow(x, p.r);
               return z / (pow(z, p.g) * p.b + p.a);
           }

           vec3 tone_mapping(vec3 color) {
               const float hdrMax = L_hdr / L_sdr; // How much HDR range before clipping. HDR modes likely need this pushed up to say 25.0.
               const float contrast = 1.2; // Use as a baseline to tune the amount of contrast the tonemapper has.
               const float shoulder = 1.0; // Likely don't need to mess with this factor, unless matching existing tonemapper is not working well..
               const float midIn = 0.18; // most games will have a {0.0 to 1.0} range for LDR so midIn should be 0.18.
               const float midOut = 0.18; // Use for LDR. For HDR10 10:10:10:2 use maybe 0.18/25.0 to start. For scRGB, I forget what a good starting point is, need to re-calculate.

               float b = f1(hdrMax, contrast, shoulder, midIn, midOut);
               float c = f2(hdrMax, contrast, shoulder, midIn, midOut);

               #define EPS 1e-6f
               float peak = max(color.r, max(color.g, color.b));
               peak = max(EPS, peak);

               vec3 ratio = color / peak;
               peak = f3(peak, vec4(contrast, shoulder, b, c) );
               // then process ratio

               // probably want send these pre-computed (so send over saturation/crossSaturation as a constant)
               float crosstalk = 4.0; // controls amount of channel crosstalk
               float saturation = contrast; // full tonal range saturation control
               float crossSaturation = contrast * 16.0; // crosstalk saturation

               float white = 1.0;

               // wrap crosstalk in transform
               ratio = pow(abs(ratio), vec3(saturation / crossSaturation));
               ratio = mix(ratio, vec3(white), vec3(pow(peak, crosstalk)));
               ratio = pow(abs(ratio), vec3(crossSaturation));

               // then apply ratio to peak
               color = peak * ratio;
               return color;
           }

           vec4 ${javaClass.name}(vec4 color) {
               color.rgb = tone_mapping(color.rgb);
               return color;
           }
        """.trimIndent()
}