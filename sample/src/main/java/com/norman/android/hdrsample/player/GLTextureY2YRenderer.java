package com.norman.android.hdrsample.player;

import android.media.MediaFormat;
import android.opengl.GLES20;
import android.util.Pair;

import com.norman.android.hdrsample.util.GLESUtil;

import java.util.HashMap;
import java.util.Map;

class GLTextureY2YRenderer extends GLTextureRenderer {

    private static final Map<Integer, Pair<float[], float[]>> YUV_TO_RGB_MATRIX_MAP = new HashMap<>();

    private static final float[] BT2020_8BIT_FULL_YUV_TO_RGB = {
            1.000000f, 1.000000f, 1.000000f, 0.000000f,
            -0.000000f, -0.164553f, 1.881400f, 0.000000f,
            1.474600f, -0.571353f, -0.000000f, 0.000000f,
            -0.740191f, 0.369396f, -0.944389f, 1.000000f
    };
    private static final float[] BT2020_8BIT_LIMITED_YUV_TO_RGB = {
            1.164384f, 1.164384f, 1.164384f, 0.000000f,
            -0.000000f, -0.187326f, 2.141772f, 0.000000f,
            1.678674f, -0.650424f, -0.000000f, 0.000000f,
            -0.915688f, 0.347458f, -1.148145f, 1.000000f
    };
    private static final float[] BT2020_10BIT_FULL_YUV_TO_RGB = {
            1.000000f, 1.000000f, 1.000000f, 0.000000f,
            -0.000000f, -0.164553f, 1.881400f, 0.000000f,
            1.474600f, -0.571353f, -0.000000f, 0.000000f,
            -0.738021f, 0.368313f, -0.941620f, 1.000000f
    };

    private static final float[] BT2020_10BIT_LIMITED_YUV_TO_RGB = {
            1.167808f, 1.167808f, 1.167808f, 0.000000f,
            -0.000000f, -0.187877f, 2.148072f, 0.000000f,
            1.683611f, -0.652337f, -0.000000f, 0.000000f,
            -0.915688f, 0.347458f, -1.148145f, 1.000000f
    };

    private static final float[] BT2020_12BIT_FULL_YUV_TO_RGB = {
            1.000000f, 1.000000f, 1.000000f, 0.000000f,
            -0.000000f, -0.164553f, 1.881400f, 0.000000f,
            1.474600f, -0.571353f, -0.000000f, 0.000000f,
            -0.737480f, 0.368043f, -0.940930f, 1.000000f
    };

    private static final float[] BT2020_12BIT_LIMITED_YUV_TO_RGB = {
            1.168664f, 1.168664f, 1.168664f, 0.000000f,
            -0.000000f, -0.188015f, 2.149647f, 0.000000f,
            1.684846f, -0.652816f, -0.000000f, 0.000000f,
            -0.915688f, 0.347458f, -1.148145f, 1.000000f
    };

    static {
        YUV_TO_RGB_MATRIX_MAP.put(8, new Pair<>(BT2020_8BIT_FULL_YUV_TO_RGB, BT2020_8BIT_LIMITED_YUV_TO_RGB));
        YUV_TO_RGB_MATRIX_MAP.put(10, new Pair<>(BT2020_10BIT_FULL_YUV_TO_RGB, BT2020_10BIT_LIMITED_YUV_TO_RGB));
        YUV_TO_RGB_MATRIX_MAP.put(12, new Pair<>(BT2020_12BIT_FULL_YUV_TO_RGB, BT2020_12BIT_LIMITED_YUV_TO_RGB));
    }

    private static final String EXTENSION_YUV_TARGET = "GL_EXT_YUV_target";


    private static final String VERTEX_SHADER = "#version 300 es\n" +
            "in vec4 position;\n" +
            "in vec4 inputTextureCoordinate;\n" +
            "uniform mat4 textureMatrix;\n" +
            "out vec2 textureCoordinate;\n" +
            "void main() {\n" +
            "    gl_Position =position;\n" +
            "    textureCoordinate =(textureMatrix*inputTextureCoordinate).xy;\n" +
            "}";

    private static final String FRAGMENT_SHADER = "#version 300 es\n" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "#extension GL_EXT_YUV_target : require\n" +
            "\n" +
            "precision highp float;\n" +
            "uniform __samplerExternal2DY2YEXT inputImageTexture;\n" +
            "uniform mat4 yuvToRgbMatrix;\n" +
            "in  vec2 textureCoordinate;\n" +
            "out vec4 outColor;\n" +
            "\n" +
            "\n" +
            "vec3 yuvToRgb(vec3 yuv){\n" +
            "    vec4 color = yuvToRgbMatrix *vec4(yuv.xyz, 1.0);\n" +
            "    return color.rgb;\n" +
            "}\n" +
            "\n" +
            "\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    vec4 yuv  = texture(inputImageTexture, textureCoordinate);\n" +
            "    vec3 rgb =  yuvToRgb(yuv.rgb);\n" +
            "    outColor = vec4(rgb, 1.0);\n" +
            "}";

    private int yuvToRgbMatrixUniform;


    private int colorRange = MediaFormat.COLOR_RANGE_LIMITED;

    private int bitDepth = 8;

    public GLTextureY2YRenderer() {
        super(TYPE_TEXTURE_EXTERNAL_OES);
    }

    @Override
    protected int onCreateProgram() {
        int programId = GLESUtil.createProgramId(VERTEX_SHADER, FRAGMENT_SHADER);
        yuvToRgbMatrixUniform = GLES20.glGetUniformLocation(programId, "yuvToRgbMatrix");
        return programId;
    }

    public void setColorRange(int colorRange) {
        this.colorRange = colorRange;
    }

    public void setBitDepth(int bitDepth) {
        this.bitDepth = bitDepth;
    }


    @Override
    void onDraw() {
        Pair<float[], float[]> pair = YUV_TO_RGB_MATRIX_MAP.get(bitDepth);
        GLES20.glUniformMatrix4fv(yuvToRgbMatrixUniform, 1, false, colorRange == MediaFormat.COLOR_RANGE_FULL ? pair.first : pair.second, 0);
    }

    public static boolean isContainY2YEXT() {
        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        return extensions.contains(EXTENSION_YUV_TARGET);
    }


}
