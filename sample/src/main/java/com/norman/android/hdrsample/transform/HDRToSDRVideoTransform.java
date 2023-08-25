package com.norman.android.hdrsample.transform;

import android.opengl.GLES20;

import com.norman.android.hdrsample.player.GLVideoTransform;
import com.norman.android.hdrsample.player.VideoOutput;
import com.norman.android.hdrsample.transform.shader.HDRToSDRShader;
import com.norman.android.hdrsample.transform.shader.MetaDataParams;
import com.norman.android.hdrsample.util.DisplayUtil;
import com.norman.android.hdrsample.util.GLESUtil;

import java.nio.FloatBuffer;

public class HDRToSDRVideoTransform extends GLVideoTransform {

    private static final int VERTEX_LENGTH = 2;

    private static final String VERTEX_SHADER = "precision mediump float;\n" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = (inputTextureCoordinate).xy;\n" +
            "}";


    private FloatBuffer textureCoordinateBuffer;
    private FloatBuffer positionCoordinateBuffer;

    private int positionCoordinateAttribute;
    private int textureCoordinateAttribute;
    private int textureUnitUniform;

    private int programId;

    private HDRToSDRShader hdrToSDRShader;

    private ScreenBrightnessObserver screenBrightnessObserver;
    private int maxDisplayLuminanceUniform;
    private int currentColorSpaceUniform;
    private int currentDisplayLuminanceUniform;
    private int maxContentLuminanceUniform;


    public HDRToSDRVideoTransform() {
        positionCoordinateBuffer = GLESUtil.createPositionFlatBuffer();
        textureCoordinateBuffer = GLESUtil.createTextureFlatBuffer();
        screenBrightnessObserver = new ScreenBrightnessObserver();
    }

    @Override
    protected void onCreate() {
        screenBrightnessObserver.listen();
    }


    @Override
    protected synchronized void onTransform() {
        int colorSpace = getInputColorSpace();
        if (colorSpace == VideoOutput.COLOR_SPACE_SDR) {
            return;
        }
        if (programId <= 0) {
            return;
        }
        clearColor();
        positionCoordinateBuffer.clear();
        textureCoordinateBuffer.clear();
        GLES20.glUseProgram(programId);
        GLESUtil.checkGLError();
        GLES20.glEnableVertexAttribArray(positionCoordinateAttribute);
        GLESUtil.checkGLError();
        GLES20.glVertexAttribPointer(positionCoordinateAttribute, VERTEX_LENGTH, GLES20.GL_FLOAT, false, 0, positionCoordinateBuffer);
        GLESUtil.checkGLError();
        GLES20.glEnableVertexAttribArray(textureCoordinateAttribute);
        GLESUtil.checkGLError();
        GLES20.glVertexAttribPointer(textureCoordinateAttribute, VERTEX_LENGTH, GLES20.GL_FLOAT, false, 0, textureCoordinateBuffer);
        GLESUtil.checkGLError();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLESUtil.checkGLError();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getInputTextureId());
        GLESUtil.checkGLError();
        GLES20.glUniform1i(textureUnitUniform, 0);
        GLESUtil.checkGLError();

        GLES20.glUniform1f(maxDisplayLuminanceUniform, DisplayUtil.getMaxLuminance());
        GLESUtil.checkGLError();
        GLES20.glUniform1i(currentColorSpaceUniform, getInputColorSpace());
        GLESUtil.checkGLError();
        GLES20.glUniform1f(maxContentLuminanceUniform, getInputMaxContentLuminance());
        GLESUtil.checkGLError();
        GLES20.glUniform1f(currentDisplayLuminanceUniform, screenBrightnessObserver.getBrightnessInfo().brightnessFloat * DisplayUtil.getMaxLuminance());
        GLESUtil.checkGLError();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLESUtil.checkGLError();
        GLES20.glDisableVertexAttribArray(positionCoordinateAttribute);
        GLESUtil.checkGLError();
        GLES20.glDisableVertexAttribArray(textureCoordinateAttribute);
        GLESUtil.checkGLError();
        GLES20.glUseProgram(0);
        GLESUtil.checkGLError();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLESUtil.checkGLError();
        success(VideoOutput.COLOR_SPACE_SDR, 0);
    }


    public synchronized void setHdrToSDRShader(HDRToSDRShader shader) {
        post(() -> {
            if (hdrToSDRShader == shader) {
                return;
            }
            hdrToSDRShader = shader;
            GLESUtil.delProgramId(programId);
            if (hdrToSDRShader != null) {
                programId = GLESUtil.createProgramId(VERTEX_SHADER, hdrToSDRShader.getCode());
                positionCoordinateAttribute = GLES20.glGetAttribLocation(programId, "position");
                GLESUtil.checkGLError();
                textureCoordinateAttribute = GLES20.glGetAttribLocation(programId, "inputTextureCoordinate");
                GLESUtil.checkGLError();
                textureUnitUniform = GLES20.glGetUniformLocation(programId, "inputImageTexture");
                GLESUtil.checkGLError();

                maxDisplayLuminanceUniform = GLES20.glGetUniformLocation(programId, MetaDataParams.MAX_DISPLAY_LUMINANCE);
                GLESUtil.checkGLError();

                currentColorSpaceUniform = GLES20.glGetUniformLocation(programId, MetaDataParams.VIDEO_COLOR_SPACE);
                GLESUtil.checkGLError();

                currentDisplayLuminanceUniform = GLES20.glGetUniformLocation(programId, MetaDataParams.CURRENT_DISPLAY_LUMINANCE);
                GLESUtil.checkGLError();

                maxContentLuminanceUniform = GLES20.glGetUniformLocation(programId, MetaDataParams.MAX_CONTENT_LUMINANCE);
                GLESUtil.checkGLError();
            }
        });
    }
}
