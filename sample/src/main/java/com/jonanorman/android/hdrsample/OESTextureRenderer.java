package com.jonanorman.android.hdrsample;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.jonanorman.android.hdrsample.util.GLESUtil;

import java.nio.FloatBuffer;


public class OESTextureRenderer {

    private static final int VERTEX_LENGTH = 2;

    private static final float POSITION_COORDINATES[] = {
            -1.0f, -1.0f,//left bottom
            1.0f, -1.0f,//right bottom
            -1.0f, 1.0f,//left top
            1.0f, 1.0f,//right top
    };


    private static final float TEXTURE_COORDINATES[] = {
            0.0f, 0.0f,//left bottom
            1.0f, 0.0f,//right bottom
            0.0f, 1.0f,//left top
            1.0f, 1.0f,//right  top
    };


    private final static String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying highp vec2 textureCoordinate;\n" +
                    "uniform samplerExternalOES inputImageTexture;\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "    vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                    "    gl_FragColor = textureColor;\n" +
                    "}";

    private static final String VERTEX_SHADER = "precision mediump float;\n" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = vec2(inputTextureCoordinate.x,1.0-inputTextureCoordinate.y);\n" +
            "}";// todo 改写textureMatrix


    private FloatBuffer textureCoordinateBuffer;
    private FloatBuffer positionCoordinateBuffer;

    private int positionCoordinateAttribute;
    private int textureCoordinateAttribute;
    private int textureUnitUniform;

    private int programId;

    private int textureId;

    private boolean hasLocation;

    private int width;
    private int height;

    private boolean release;

    public OESTextureRenderer() {
        positionCoordinateBuffer = GLESUtil.createDirectFloatBuffer(POSITION_COORDINATES);
        textureCoordinateBuffer = GLESUtil.createDirectFloatBuffer(TEXTURE_COORDINATES);
        this.programId = GLESUtil.createProgramId(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public void setSurfaceSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    public void render() {
        if (release) {
            return;
        }
        positionCoordinateBuffer.clear();
        textureCoordinateBuffer.clear();
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glUseProgram(programId);
        GLES20.glViewport(0, 0, width, height);
        if (!hasLocation) {
            hasLocation = true;
            positionCoordinateAttribute = GLES20.glGetAttribLocation(programId, "position");
            textureCoordinateAttribute = GLES20.glGetAttribLocation(programId, "inputTextureCoordinate");
            textureUnitUniform = GLES20.glGetUniformLocation(programId, "inputImageTexture");
        }
        GLES20.glEnableVertexAttribArray(positionCoordinateAttribute);
        GLES20.glVertexAttribPointer(positionCoordinateAttribute, VERTEX_LENGTH, GLES20.GL_FLOAT, false, 0, positionCoordinateBuffer);
        if (textureCoordinateAttribute >= 0) {
            GLES20.glEnableVertexAttribArray(textureCoordinateAttribute);
            GLES20.glVertexAttribPointer(textureCoordinateAttribute, VERTEX_LENGTH, GLES20.GL_FLOAT, false, 0, textureCoordinateBuffer);
        }
        if (textureUnitUniform >= 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES20.glUniform1i(textureUnitUniform, 0);
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(positionCoordinateAttribute);
        if (textureCoordinateAttribute >= 0) {
            GLES20.glDisableVertexAttribArray(textureCoordinateAttribute);
        }
        GLES20.glUseProgram(0);
        GLES20.glDisable(GLES20.GL_BLEND);
        if (textureUnitUniform >= 0) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        }
    }


    public void release() {
        if (release) {
            return;
        }
        release = true;
        GLESUtil.delProgramId(programId);
    }


}
