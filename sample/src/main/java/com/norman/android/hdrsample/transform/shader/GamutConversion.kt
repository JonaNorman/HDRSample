package com.norman.android.hdrsample.transform.shader

import com.norman.android.hdrsample.opengl.GLShaderCode

object GamutConversion : GLShaderCode() {
    override val code: String
        get() = """
        #define BT2020_TO_BT709_MAT3  mat3(1.660491, -0.12455047, -0.01815076, \
                                                            -0.58764114, 1.1328999, -0.1005789, \
                                                            -0.07284986, -0.00834942, 1.11872966)

        #define BT709_TO_BT2020_MAT3  mat3(0.6274040, 0.0690970, 0.0163916, \
                                                        0.3292820, 0.9195400, 0.0880132, \
                                                        0.0433136, 0.0113612, 0.8955950)

        #define BT2020_TO_XYZ_MAT3  mat3(0.636958,0.262700 , 0.000000, \
                                                        0.144617,0.677998,0.028073, \
                                                        0.168881,0.059302, 1.060985)


        #define XYZ_TO_BT2020_MAT3  mat3(1.716651,-0.666684 , 0.017640, \
                                                        -0.355671,1.616481,-0.042771, \
                                                        -0.253366,0.015769, 0.942103)

        #define XYZ_TO_BT709_MAT3  mat3(3.240970,-0.969244 , 0.055630, \
                                                        -1.537383,1.875968,-0.203977, \
                                                       -0.498611,0.041555, 1.056972)

        vec3 BT2020_TO_BT709(vec3 x)
        {
            return BT2020_TO_BT709_MAT3 * x;
        }


        vec3 BT709_TO_BT2020(vec3 x)
        {
            return BT709_TO_BT2020_MAT3 * x;
        }



        vec3 BT2020_TO_XYZ(vec3 x)
        {
            return BT2020_TO_XYZ_MAT3 * x;
        }

        vec3 XYZ_TO_BT709(vec3 x)
        {
            return XYZ_TO_BT709_MAT3 * x;
        }

        vec3 XYZ_TO_BT2020(vec3 x)
        {
            return XYZ_TO_BT2020_MAT3 * x;
        }


        vec3 XYZ_TO_xyY(vec3 XYZ) {
            float divisor = XYZ.x + XYZ.y + XYZ.z;
            if (divisor == 0.0) divisor = 1e-6;

            float x = XYZ.x / divisor;
            float y = XYZ.y / divisor;

            return vec3(x, y, XYZ.z);
        }

        vec3 xyY_TO_XYZ(vec3 xyY) {
            float multiplo = xyY.z / max(xyY.y, 1e-6);
            float z = 1.0 - xyY.x - xyY.y;
            float X = xyY.x * multiplo;
            float Z = z * multiplo;
            return vec3(X, xyY.z, Z);
        } 
        """.trimIndent()

}