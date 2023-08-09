package com.norman.android.hdrsample.player;

import android.opengl.GLES20;

import com.norman.android.hdrsample.util.ColorMatrixUtil;
import com.norman.android.hdrsample.util.GLESUtil;

class GLY2YExtensionRenderer extends GLTextureRenderer {

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


    private @ColorMatrixUtil.ColorRange int colorRange = ColorMatrixUtil.COLOR_RANGE_LIMITED;

    private int bitDepth = 8;

    public GLY2YExtensionRenderer() {
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
        GLES20.glUniformMatrix4fv(yuvToRgbMatrixUniform, 1, false, ColorMatrixUtil.getYuvToRgbMatrix(bitDepth,colorRange), 0);
    }

    public static boolean isContainY2YEXT() {
        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        return extensions.contains(EXTENSION_YUV_TARGET);
    }


}
