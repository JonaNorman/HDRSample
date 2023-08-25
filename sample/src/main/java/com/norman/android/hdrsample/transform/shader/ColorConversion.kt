package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode
import com.norman.android.hdrsample.transform.shader.MetaDataParams.EPSILON
import com.norman.android.hdrsample.transform.shader.MetaDataParams.HDR_REFERENCE_WHITE
import com.norman.android.hdrsample.transform.shader.MetaDataParams.PI

object ColorConversion : GLShaderCode() {

    const val methodBt2020ToBt709 = "BT2020_TO_BT709"
    const val methodBt709ToBt2020 = "BT709_TO_BT2020"

    const val methodBt2020ToXYZ = "BT2020_TO_XYZ"
    const val methodXYZToBt2020 = "XYZ_TO_BT2020"

    const val methodXYZToBt709 = "XYZ_TO_BT709"

    const val methodXYZD65ToXYZD50 = "XYZD65_TO_XYZD50"
    const val methodXYZD50ToXYZD65 = "XYZD50_TO_XYZD65"

    const val methodXYZToLab = "XYZ_TO_LAB"
    const val methodLabToXYZ = "LAB_TO_XYZ"

    const val methodBt2020ToLab = "BT2020_TO_LAB"
    const val methodLabToBT2020 = "LAB_TO_BT2020"

    const val methodLabToLch = "LAB_to_LCH"
    const val methodLchToLab = "LCH_to_LAB"

    const val methodXYZToxyY = "XYZ_TO_xyY"
    const val methodxyYToXYZ = "xyY_TO_XYZ"


    override val code: String
        get() = """
        const mat3 BT2020_TO_BT709_MAT3 = mat3(1.660491,-0.12455047,-0.01815076,
                                              -0.58764114,1.1328999,-0.1005789,
                                              -0.07284986,-0.00834942,1.11872966);

        const mat3 BT709_TO_BT2020_MAT3 = mat3(0.6274040,0.0690970,0.0163916,
                                              0.3292820,0.9195400,0.0880132,
                                              0.0433136,0.0113612,0.8955950);

        const mat3 BT2020_TO_XYZ_MAT3 = mat3(0.636958,0.262700,0.000000,
                                            0.144617,0.677998,0.028073,
                                            0.168881,0.059302,1.060985);


        const mat3 XYZ_TO_BT2020_MAT3 = mat3(1.716651,-0.666684,0.017640,
                                           -0.355671,1.616481,-0.042771,
                                           -0.253366,0.015769,0.942103);

        const mat3 XYZ_TO_BT709_MAT3 = mat3(3.240970,-0.969244,0.055630,
                                           -1.537383,1.875968,-0.203977,
                                           -0.498611,0.041555, 1.056972);
                                                       
        const mat3 XYZD65_TO_XYZD50_MAT3 = mat3(1.047930,0.029628,-0.009243,
                                               0.022947,0.990434,0.015055,
                                               -0.050192,-0.017074,0.751874);   
               
                                                    
        const mat3 XYZD50_TO_XYZD65_MAT3 = mat3(0.955473, -0.028370,0.012314,
                                                -0.023099,1.009995,-0.020508,
                                                0.063259,0.021041,1.330366); 
        
        #define  LAB_DELTA  6.0 / 29.0
        #define  LAB_DELTAC  LAB_DELTA * 2.0 / 3.0                                                                                  

        vec3 $methodBt2020ToBt709(vec3 x)
        {
            return BT2020_TO_BT709_MAT3 * x;
        }

        vec3 $methodBt709ToBt2020(vec3 x)
        {
            return BT709_TO_BT2020_MAT3 * x;
        }
        
        vec3 $methodXYZToBt709(vec3 x)
        {
            return XYZ_TO_BT709_MAT3 * x;
        }

        vec3 $methodBt2020ToXYZ(vec3 x)
        {
            return BT2020_TO_XYZ_MAT3 * x;
        }

        vec3 $methodXYZToBt2020(vec3 x)
        {
            return XYZ_TO_BT2020_MAT3 * x;
        }
        
        vec3 $methodXYZD65ToXYZD50(vec3 x) {
          
           return XYZD65_TO_XYZD50_MAT3 * x;
        }
        
        vec3 $methodXYZD50ToXYZD65(vec3 x) {
           return XYZD50_TO_XYZD65_MAT3 * x;
        }
        

        float labf1(float x) {
            return x > pow(LAB_DELTA, 3.0) ?
            sign(x) * pow(abs(x), 1.0 / 3.0) :
            LAB_DELTAC + x / (3.0 * pow(LAB_DELTA, 2.0));
        }

        float labf2(float x) {
            return x > LAB_DELTA ?
            pow(x, 3.0) :
            (x - LAB_DELTAC) * (3.0 * pow(LAB_DELTA, 2.0));
        }

        vec3 $methodXYZToLab(vec3 XYZ,vec3 XYZ_ref) {
            float X = XYZ.x;
            float Y = XYZ.y;
            float Z = XYZ.z;
            X = labf1(X / XYZ_ref.x);
            Y = labf1(Y / XYZ_ref.y);
            Z = labf1(Z / XYZ_ref.z);
            float L = 116.0 * Y - 16.0;
            float a = 500.0 * (X - Y);
            float b = 200.0 * (Y - Z);
            return vec3(L, a, b);
        }

        vec3 $methodLabToXYZ(vec3 Lab,vec3 XYZ_ref) {
            float L = Lab.x;
            float a = Lab.y;
            float b = Lab.z;
            float Y = (L + 16.0) / 116.0;
            float X = Y + a / 500.0;
            float Z = Y - b / 200.0;
            X = labf2(X) * XYZ_ref.x;
            Y = labf2(Y) * XYZ_ref.y;
            Z = labf2(Z) * XYZ_ref.z;
            return vec3(X, Y, Z);
        }
        

        vec3 $methodBt2020ToLab(vec3 color) {
           color  = $methodBt2020ToXYZ(color);
           color  = $methodXYZD65ToXYZD50(color);
           color  = $methodXYZToLab(color,$methodBt2020ToXYZ(vec3($HDR_REFERENCE_WHITE)));
           return color;
        }

        vec3 $methodLabToBT2020(vec3 color) {
           color  = $methodLabToXYZ(color,$methodBt2020ToXYZ(vec3($HDR_REFERENCE_WHITE)));
           color  = $methodXYZD50ToXYZD65(color);
           color  = $methodXYZToBt2020(color);
           return color;
        }
       
        vec3 $methodLabToLch(vec3 Lab) {
           float a = Lab.y;
           float b = Lab.z;
           float C = length(vec2(a, b));
           float H = 0.0;
           if (!(abs(a) < $EPSILON && abs(b) < $EPSILON)) {
               H = atan(b, a);
               H = H * 180.0 / $PI;
               H = mod((mod(H, 360.0) + 360.0), 360.0);
           }
           return vec3(Lab.x, C, H);
        }

        vec3 $methodLchToLab(vec3 LCH) {
           float C = max(LCH.y, 0.0);
           float H = LCH.z * $PI / 180.0;
           float a = C * cos(H);
           float b = C * sin(H);
           return vec3(LCH.x, a, b);
        }
       
        vec3 $methodXYZToxyY(vec3 XYZ) {
            float X = XYZ.x;
            float Y = XYZ.y;
            float Z = XYZ.z;

            float divisor = X + Y + Z;
            if (divisor == 0.0) divisor = 1e-6;

            float x = X / divisor;
            float y = Y / divisor;

            return vec3(x, y, Y);
        }

        vec3 $methodxyYToXYZ(vec3 xyY) {
            float x = xyY.x;
            float y = xyY.y;
            float Y = xyY.z;

            float multiplo = Y / max(y, 1e-6);

            float z = 1.0 - x - y;
            float X = x * multiplo;
            float Z = z * multiplo;

            return vec3(X, Y, Z);
        } 
        """.trimIndent()

}