package com.norman.android.hdrsample.player;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.norman.android.hdrsample.opengl.GLMatrix;
import com.norman.android.hdrsample.util.BufferUtil;
import com.norman.android.hdrsample.util.GLESUtil;

import java.nio.FloatBuffer;

class GLExternalTextureRenderer extends GLRenderer {

    private static final int VERTEX_LENGTH = 2;

    private static final float[] POSITION_COORDINATES = {
            -1.0f, -1.0f,//left bottom
            1.0f, -1.0f,//right bottom
            -1.0f, 1.0f,//left top
            1.0f, 1.0f,//right top
    };


    private static final float[] TEXTURE_COORDINATES = {
            0.0f, 0.0f,//left bottom
            1.0f, 0.0f,//right bottom
            0.0f, 1.0f,//left top
            1.0f, 1.0f,//right  top
    };


    private static final String FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\n" +
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
            "uniform mat4 textureMatrix;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = (textureMatrix*inputTextureCoordinate).xy;\n" +
            "}";


    private final FloatBuffer textureCoordinateBuffer;
    private final FloatBuffer positionCoordinateBuffer;

    private int positionCoordinateAttribute;
    private int textureCoordinateAttribute;
    private int textureUnitUniform;
    private int textureMatrixUniform;

    private int programId;


    private int textureId;

    private final GLMatrix textureMatrix = new GLMatrix();


    public GLExternalTextureRenderer() {
        positionCoordinateBuffer = BufferUtil.createDirectFloatBuffer(POSITION_COORDINATES);
        textureCoordinateBuffer = BufferUtil.createDirectFloatBuffer(TEXTURE_COORDINATES);
    }


    @Override
    public void onCreate() {
        this.programId = GLESUtil.createProgramId(VERTEX_SHADER, FRAGMENT_SHADER);
        positionCoordinateAttribute = GLES20.glGetAttribLocation(programId, "position");
        textureCoordinateAttribute = GLES20.glGetAttribLocation(programId, "inputTextureCoordinate");
        textureUnitUniform = GLES20.glGetUniformLocation(programId, "inputImageTexture");
        textureMatrixUniform = GLES20.glGetUniformLocation(programId, "textureMatrix");
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }


    public GLMatrix getTextureMatrix() {
        return textureMatrix;
    }

    @Override
    void onRender() {
        positionCoordinateBuffer.clear();
        textureCoordinateBuffer.clear();
        GLES20.glUseProgram(programId);
        GLES20.glEnableVertexAttribArray(positionCoordinateAttribute);
        GLES20.glVertexAttribPointer(positionCoordinateAttribute, VERTEX_LENGTH, GLES20.GL_FLOAT, false, 0, positionCoordinateBuffer);
        GLES20.glEnableVertexAttribArray(textureCoordinateAttribute);
        GLES20.glVertexAttribPointer(textureCoordinateAttribute, VERTEX_LENGTH, GLES20.GL_FLOAT, false, 0, textureCoordinateBuffer);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(textureUnitUniform, 0);
        GLES20.glUniformMatrix4fv(textureMatrixUniform, 1, false, textureMatrix.get(), 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(positionCoordinateAttribute);
        GLES20.glDisableVertexAttribArray(textureCoordinateAttribute);
        GLES20.glUseProgram(0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }
}
