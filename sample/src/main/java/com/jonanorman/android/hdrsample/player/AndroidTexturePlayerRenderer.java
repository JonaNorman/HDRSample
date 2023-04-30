package com.jonanorman.android.hdrsample.player;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.jonanorman.android.hdrsample.util.GLESUtil;
import com.jonanorman.android.hdrsample.util.Matrix4;

import java.nio.FloatBuffer;

public class AndroidTexturePlayerRenderer {

    private static final int VERTEX_LENGTH = 2;
    private static final String EXTENSION_YUV_TARGET = "GL_EXT_YUV_target";


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


    private FloatBuffer textureCoordinateBuffer;
    private FloatBuffer positionCoordinateBuffer;

    private int positionCoordinateAttribute;
    private int textureCoordinateAttribute;
    private int textureUnitUniform;
    private int textureMatrixUniform;
    private int contentLuminanceUniform;
    private int screenLuminanceUniform;

    private int programId;

    private int textureId;

    private boolean hasLocation;

    private int width;
    private int height;
    private float contentLuminance;
    private float screenLuminance;

    private boolean release;

    private Matrix4 textureMatrix = new Matrix4();

    public AndroidTexturePlayerRenderer() {
        positionCoordinateBuffer = GLESUtil.createDirectFloatBuffer(POSITION_COORDINATES);
        textureCoordinateBuffer = GLESUtil.createDirectFloatBuffer(TEXTURE_COORDINATES);
        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        if (extensions != null && extensions.contains(EXTENSION_YUV_TARGET)){
            this.programId = GLESUtil.createProgramId(Shader.EXT_2DY2Y_VERTEX_SHADER, Shader.EXT_2DY2Y_FRAGMENT_SHADER);
        }else {
            this.programId = GLESUtil.createProgramId(Shader.OES_VERTEX_SHADER, Shader.OES_FRAGMENT_SHADER);
        }

    }

    public void setSurfaceSize(int width, int height) {
        this.width = width;
        this.height = height;
    }


    public void setContentLuminance(int contentLuminance){
        this.contentLuminance = contentLuminance;
    }

    public void setScreenLuminance(float screenLuminance) {
        this.screenLuminance = screenLuminance <=0?100: screenLuminance;
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }


    public Matrix4 getTextureMatrix() {
        return textureMatrix;
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
            textureMatrixUniform = GLES20.glGetUniformLocation(programId, "textureMatrix");
            screenLuminanceUniform = GLES20.glGetUniformLocation(programId, "screenLuminance");
            contentLuminanceUniform = GLES20.glGetUniformLocation(programId, "contentLuminance");
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
        GLES20.glUniformMatrix4fv(textureMatrixUniform, 1, false, textureMatrix.get(), 0);
        GLES20.glUniform1f(contentLuminanceUniform, contentLuminance);
        GLES20.glUniform1f(screenLuminanceUniform, screenLuminance);
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
