package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object GamutMapAdaptiveL0 : GLShaderCode() {

    /**
     *    个人理解：BT2020转BT709超过边界的值通过保持亮度不同自适应找到一个在色域内的点作为边界值，也属于裁剪方法的一种
     *   // https://bottosson.github.io/posts/gamutclipping/
     *   // https://wenku.baidu.com/view/7ffc40bf02768e9950e738b8?bfetype=new&_wkts_=1692340094511
     */

    override val code: String
        get() = """
           #define cbrt(x) (sign(x) * pow(abs(x), 1.0 / 3.0))
           #define FLT_MAX 3.402823466e+38

           vec3 linear_srgb_to_oklab(vec3 c)
           {
           	float l = 0.4122214708 * c.r + 0.5363325363 * c.g + 0.0514459929 * c.b;
           	float m = 0.2119034982 * c.r + 0.6806995451 * c.g + 0.1073969566 * c.b;
           	float s = 0.0883024619 * c.r + 0.2817188376 * c.g + 0.6299787005 * c.b;

           	float l_ = cbrt(l);
           	float m_ = cbrt(m);
           	float s_ = cbrt(s);

           	return vec3(
           		0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_,
           		1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_,
           		0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_
           	);
           }

           vec3 oklab_to_linear_srgb(vec3 c)
           {
               float l_ = c.x + 0.3963377774 * c.y + 0.2158037573 * c.b;
               float m_ = c.x - 0.1055613458 * c.y - 0.0638541728 * c.b;
               float s_ = c.x - 0.0894841775 * c.y - 1.2914855480 * c.b;

               float l = l_ * l_ * l_;
               float m = m_ * m_ * m_;
               float s = s_ * s_ * s_;

               return vec3(
                   +4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s,
                   -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s,
                   -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s
               );
           }

           // Finds the maximum saturation possible for a given hue that fits in sRGB
           // Saturation here is defined as S = C/L
           // a and b must be normalized so a^2 + b^2 == 1
           float compute_max_saturation(float a, float b)
           {
               // Max saturation will be when one of r, g or b goes below zero.

               // Select different coefficients depending on which component goes below zero first
               float k0, k1, k2, k3, k4, wl, wm, ws;

               if (-1.88170328 * a - 0.80936493 * b > 1)
               {
                   // Red component
                   k0 = +1.19086277; k1 = +1.76576728; k2 = +0.59662641; k3 = +0.75515197; k4 = +0.56771245;
                   wl = +4.0767416621; wm = -3.3077115913; ws = +0.2309699292;
               }
               else if (1.81444104 * a - 1.19445276 * b > 1)
               {
                   // Green component
                   k0 = +0.73956515; k1 = -0.45954404; k2 = +0.08285427; k3 = +0.12541070; k4 = +0.14503204;
                   wl = -1.2684380046; wm = +2.6097574011; ws = -0.3413193965;
               }
               else
               {
                   // Blue component
                   k0 = +1.35733652; k1 = -0.00915799; k2 = -1.15130210; k3 = -0.50559606; k4 = +0.00692167;
                   wl = -0.0041960863; wm = -0.7034186147; ws = +1.7076147010;
               }

               // Approximate max saturation using a polynomial:
               float S = k0 + k1 * a + k2 * b + k3 * a * a + k4 * a * b;

               // Do one step Halley's method to get closer
               // this gives an error less than 10e6, except for some blue hues where the dS/dh is close to infinite
               // this should be sufficient for most applications, otherwise do two/three steps

               float k_l = +0.3963377774 * a + 0.2158037573 * b;
               float k_m = -0.1055613458 * a - 0.0638541728 * b;
               float k_s = -0.0894841775 * a - 1.2914855480 * b;

               {
                   float l_ = 1.0 + S * k_l;
                   float m_ = 1.0 + S * k_m;
                   float s_ = 1.0 + S * k_s;

                   float l = l_ * l_ * l_;
                   float m = m_ * m_ * m_;
                   float s = s_ * s_ * s_;

                   float l_dS = 3.0 * k_l * l_ * l_;
                   float m_dS = 3.0 * k_m * m_ * m_;
                   float s_dS = 3.0 * k_s * s_ * s_;

                   float l_dS2 = 6.0 * k_l * k_l * l_;
                   float m_dS2 = 6.0 * k_m * k_m * m_;
                   float s_dS2 = 6.0 * k_s * k_s * s_;

                   float f  = wl * l     + wm * m     + ws * s;
                   float f1 = wl * l_dS  + wm * m_dS  + ws * s_dS;
                   float f2 = wl * l_dS2 + wm * m_dS2 + ws * s_dS2;

                   S = S - f * f1 / (f1*f1 - 0.5 * f * f2);
               }

               return S;
           }

           // finds L_cusp and C_cusp for a given hue
           // a and b must be normalized so a^2 + b^2 == 1
           // struct LC { float L; float C; };
           vec2 find_cusp(float a, float b)
           {
               // First, find the maximum saturation (saturation S = C/L)
               float S_cusp = compute_max_saturation(a, b);

           	// Convert to linear sRGB to find the first point where at least one of r,g or b >= 1:
               vec3 rgb_at_max = oklab_to_linear_srgb(vec3(1, S_cusp * a, S_cusp * b));
               float L_cusp = cbrt(1.0 / max(max(rgb_at_max.r, rgb_at_max.g), rgb_at_max.b));
               float C_cusp = L_cusp * S_cusp;

               return vec2(L_cusp , C_cusp);
           }

           // Finds intersection of the line defined by
           // L = L0 * (1 - t) + t * L1;
           // C = t * C1;
           // a and b must be normalized so a^2 + b^2 == 1
           float find_gamut_intersection(float a, float b, float L1, float C1, float L0)
           {
               // Find the cusp of the gamut triangle
               vec2 cusp = find_cusp(a, b);

               // Find the intersection for upper and lower half seprately
               float t;
               if (((L1 - L0) * cusp.y - (cusp.x - L0) * C1) <= 0.0)
               {
                   // Lower half

                   t = cusp.y * L0 / (C1 * cusp.x + cusp.y * (L0 - L1));
               }
               else
               {
                   // Upper half

                   // First intersect with triangle
                   t = cusp.y * (L0 - 1.0) / (C1 * (cusp.x - 1.0) + cusp.y * (L0 - L1));

                   // Then one step Halley's method
                   {
                       float dL = L1 - L0;
                       float dC = C1;

                       float k_l = +0.3963377774 * a + 0.2158037573 * b;
                       float k_m = -0.1055613458 * a - 0.0638541728 * b;
                       float k_s = -0.0894841775 * a - 1.2914855480 * b;

                       float l_dt = dL + dC * k_l;
                       float m_dt = dL + dC * k_m;
                       float s_dt = dL + dC * k_s;


                       // If higher accuracy is required, 2 or 3 iterations of the following block can be used:
                       {
                           float L = L0 * (1.0 - t) + t * L1;
                           float C = t * C1;

                           float l_ = L + C * k_l;
                           float m_ = L + C * k_m;
                           float s_ = L + C * k_s;

                           float l = l_ * l_ * l_;
                           float m = m_ * m_ * m_;
                           float s = s_ * s_ * s_;

                           float ldt = 3 * l_dt * l_ * l_;
                           float mdt = 3 * m_dt * m_ * m_;
                           float sdt = 3 * s_dt * s_ * s_;

                           float ldt2 = 6 * l_dt * l_dt * l_;
                           float mdt2 = 6 * m_dt * m_dt * m_;
                           float sdt2 = 6 * s_dt * s_dt * s_;

                           float r = 4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s - 1;
                           float r1 = 4.0767416621 * ldt - 3.3077115913 * mdt + 0.2309699292 * sdt;
                           float r2 = 4.0767416621 * ldt2 - 3.3077115913 * mdt2 + 0.2309699292 * sdt2;

                           float u_r = r1 / (r1 * r1 - 0.5 * r * r2);
                           float t_r = -r * u_r;

                           float g = -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s - 1;
                           float g1 = -1.2684380046 * ldt + 2.6097574011 * mdt - 0.3413193965 * sdt;
                           float g2 = -1.2684380046 * ldt2 + 2.6097574011 * mdt2 - 0.3413193965 * sdt2;

                           float u_g = g1 / (g1 * g1 - 0.5 * g * g2);
                           float t_g = -g * u_g;

                           float b = -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s - 1;
                           float b1 = -0.0041960863 * ldt - 0.7034186147 * mdt + 1.7076147010 * sdt;
                           float b2 = -0.0041960863 * ldt2 - 0.7034186147 * mdt2 + 1.7076147010 * sdt2;

                           float u_b = b1 / (b1 * b1 - 0.5 * b * b2);
                           float t_b = -b * u_b;

                           t_r = u_r >= 0.0 ? t_r : FLT_MAX;
                           t_g = u_g >= 0.0 ? t_g : FLT_MAX;
                           t_b = u_b >= 0.0 ? t_b : FLT_MAX;

                           t += min(t_r, min(t_g, t_b));
                       }
                   }
               }

               return t;
           }

           // float clamp(float x, float min, float max)
           // {
           // 	if (x < min)
           // 		return min;
           // 	if (x > max)
           // 		return max;

           // 	return x;
           // }

           // float sgn(float x)
           // {
           // 	return (float)(0.0 < x) - (float)(x < 0.0);
           // }

           vec3 gamut_clip_preserve_chroma(vec3 rgb)
           {
               if (rgb.r < 1 && rgb.g < 1 && rgb.b < 1 && rgb.r > 0 && rgb.g > 0 && rgb.b > 0)
                   return rgb;

               vec3 lab = linear_srgb_to_oklab(rgb);

               float L = lab.x;
               float eps = 0.00001;
               float C = max(eps, sqrt(lab.y * lab.y + lab.z * lab.z));
               float a_ = lab.y / C;
               float b_ = lab.z / C;

               float L0 = clamp(L, 0, 1);

               float t = find_gamut_intersection(a_, b_, L, C, L0);
               float L_clipped = L0 * (1 - t) + t * L;
               float C_clipped = t * C;

               return oklab_to_linear_srgb(vec3(L_clipped, C_clipped * a_, C_clipped * b_));
           }

           vec3 gamut_clip_project_to_0_5(vec3 rgb)
           {
               if (rgb.r < 1 && rgb.g < 1 && rgb.b < 1 && rgb.r > 0 && rgb.g > 0 && rgb.b > 0)
                   return rgb;

               vec3 lab = linear_srgb_to_oklab(rgb);

               float L = lab.x;
               float eps = 0.00001;
               float C = max(eps, sqrt(lab.y * lab.y + lab.z * lab.z));
               float a_ = lab.y / C;
               float b_ = lab.z / C;

               float L0 = 0.5;

               float t = find_gamut_intersection(a_, b_, L, C, L0);
               float L_clipped = L0 * (1 - t) + t * L;
               float C_clipped = t * C;

               return oklab_to_linear_srgb(vec3(L_clipped, C_clipped * a_, C_clipped * b_));
           }

           vec3 gamut_clip_project_to_L_cusp(vec3 rgb)
           {
               if (rgb.r < 1 && rgb.g < 1 && rgb.b < 1 && rgb.r > 0 && rgb.g > 0 && rgb.b > 0)
                   return rgb;

               vec3 lab = linear_srgb_to_oklab(rgb);

               float L = lab.x;
               float eps = 0.00001;
               float C = max(eps, sqrt(lab.y * lab.y + lab.z * lab.z));
               float a_ = lab.y / C;
               float b_ = lab.z / C;

               // The cusp is computed here and in find_gamut_intersection, an optimized solution would only compute it once.
               vec2 cusp = find_cusp(a_, b_);

               float L0 = cusp.x;

               float t = find_gamut_intersection(a_, b_, L, C, L0);

               float L_clipped = L0 * (1 - t) + t * L;
               float C_clipped = t * C;

               return oklab_to_linear_srgb(vec3(L_clipped, C_clipped * a_, C_clipped * b_));
           }

           vec3 gamut_clip_adaptive_L0_0_5(vec3 rgb, float alpha)
           {
               if (alpha < 0) alpha = 0.05;
               if (rgb.r < 1 && rgb.g < 1 && rgb.b < 1 && rgb.r > 0 && rgb.g > 0 && rgb.b > 0)
                   return rgb;

               vec3 lab = linear_srgb_to_oklab(rgb);

               float L = lab.x;
               float eps = 0.00001;
               float C = max(eps, sqrt(lab.y * lab.y + lab.z * lab.z));
               float a_ = lab.y / C;
               float b_ = lab.z / C;

               float Ld = L - 0.5;
               float e1 = 0.5 + abs(Ld) + alpha * C;
               float L0 = 0.5*(1.0 + sign(Ld)*(e1 - sqrt(e1*e1 - 2.0 *abs(Ld))));

               float t = find_gamut_intersection(a_, b_, L, C, L0);
               float L_clipped = L0 * (1.0 - t) + t * L;
               float C_clipped = t * C;

               return oklab_to_linear_srgb(vec3(L_clipped, C_clipped * a_, C_clipped * b_));
           }

           vec3 gamut_clip_adaptive_L0_L_cusp(vec3 rgb, float alpha)
           {
               if (alpha < 0) alpha = 0.05;
               if (rgb.r < 1 && rgb.g < 1 && rgb.b < 1 && rgb.r > 0 && rgb.g > 0 && rgb.b > 0)
                   return rgb;

               vec3 lab = linear_srgb_to_oklab(rgb);

               float L = lab.x;
               float eps = 0.00001;
               float C = max(eps, sqrt(lab.y * lab.y + lab.z * lab.z));
               float a_ = lab.y / C;
               float b_ = lab.z / C;

               // The cusp is computed here and in find_gamut_intersection, an optimized solution would only compute it once.
               vec2 cusp = find_cusp(a_, b_);

               float Ld = L - cusp.x;
               float k = 2.0 * (Ld > 0 ? 1.0 - cusp.x : cusp.x);

               float e1 = 0.5*k + abs(Ld) + alpha * C/k;
               float L0 = cusp.x + 0.5 * (sign(Ld) * (e1 - sqrt(e1 * e1 - 2.0 * k * abs(Ld))));

               float t = find_gamut_intersection(a_, b_, L, C, L0);
               float L_clipped = L0 * (1.0 - t) + t * L;
               float C_clipped = t * C;

               return oklab_to_linear_srgb(vec3(L_clipped, C_clipped * a_, C_clipped * b_));
           }


           vec4 ${javaClass.name}(vec4 color) {
           
               vec4 bt709Color = BT2020_TO_BT709(color.rgb);
               color.rgb = gamut_clip_adaptive_L0_L_cusp(bt709Color, -1.0);
               return color;
           }
        """.trimIndent()
}