package com.norman.android.hdrsample.todo;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.norman.android.hdrsample.handler.Future;
import com.norman.android.hdrsample.handler.MessageHandler;
import com.norman.android.hdrsample.player.GLVideoTransform;
import com.norman.android.hdrsample.util.BufferUtil;
import com.norman.android.hdrsample.util.GLESUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class CubeLutVideoTransform extends GLVideoTransform {

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

    private static final String VERTEX_SHADER = "#version 300 es\n" +
            "in vec4 position;\n" +
            "in vec4 inputTextureCoordinate;\n" +
            "out vec2 textureCoordinate;\n" +
            "void main() {\n" +
            "    gl_Position =position;\n" +
            "    textureCoordinate =(inputTextureCoordinate).xy;\n" +
            "}";


    private static final String FRAGMENT_SHADER = "#version 300 es\n" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision highp float;\n" +
            "precision highp sampler3D;\n" +
            "in  vec2 textureCoordinate;\n" +
            "out vec4 outColor;\n" +
            "\n" +
            "uniform sampler2D inputImageTexture;\n" +
            "uniform sampler3D cubeLutTexture;\n" +
            "uniform float cubeLutSize;\n" +
            "\n" +
            "\n" +
            "void main() {\n" +
            "\n" +
            "    // Based on \\\\\\\\GPU Gems 2 — Chapter 24. Using Lookup Tables to Accelerate Color Transformations\\\\\\\\\n" +
            "    // More info and credits @ http://http.developer.nvidia.com/GPUGems2/gpugems2_chapter24.html\n" +
            "    vec4 rawColor = texture(inputImageTexture, textureCoordinate);\n" +
            "    vec3 scale = vec3((cubeLutSize - 1.0) / cubeLutSize);\n" +
            "    vec3 offset = vec3(1.0 / (2.0 * cubeLutSize));\n" +
            "    outColor.rgb = texture(cubeLutTexture, scale * rawColor.rgb + offset).rgb;\n" +
            "    outColor.a = rawColor.a;\n" +
            "}";


    private int lutTextureId;
    private int lutSize;

    private boolean lutEnable;

    private CubeLut3D currentCube;

    private FloatBuffer textureCoordinateBuffer;
    private FloatBuffer positionCoordinateBuffer;

    private int positionCoordinateAttribute;
    private int textureCoordinateAttribute;
    private int textureUnitUniform;

    private int cubeLutTextureUniform;

    private int cubeLutSizeUniform;

    private int programId;


    private volatile Future<CubeLut3D> lut3DFuture;


    public CubeLutVideoTransform() {
        positionCoordinateBuffer = BufferUtil.createDirectFloatBuffer(POSITION_COORDINATES);
        textureCoordinateBuffer = BufferUtil.createDirectFloatBuffer(TEXTURE_COORDINATES);
    }

    @Override
    protected void onCreate() {
        this.programId = GLESUtil.createProgramId(VERTEX_SHADER, FRAGMENT_SHADER);
        positionCoordinateAttribute = GLES20.glGetAttribLocation(programId, "position");
        GLESUtil.checkGLError();
        textureCoordinateAttribute = GLES20.glGetAttribLocation(programId, "inputTextureCoordinate");
        GLESUtil.checkGLError();
        textureUnitUniform = GLES20.glGetUniformLocation(programId, "inputImageTexture");
        GLESUtil.checkGLError();
        cubeLutTextureUniform = GLES20.glGetUniformLocation(programId, "cubeLutTexture");
        GLESUtil.checkGLError();
        cubeLutSizeUniform = GLES20.glGetUniformLocation(programId, "cubeLutSize");
        GLESUtil.checkGLError();
    }

    @Override
    protected void onTransform() {
        Future<CubeLut3D> future = lut3DFuture;
        if (future != null && future.get() != currentCube) {
            currentCube = future.get();
            GLESUtil.delTextureId(lutTextureId);
            GLESUtil.checkGLError();
            lutTextureId = 0;
            lutSize = 0;
            lutEnable = false;
            if (currentCube != null) {
                ByteBuffer byteBuffer = currentCube.buffer;
                byteBuffer.rewind();
                lutTextureId = GLESUtil.create3DTextureId();
                GLESUtil.checkGLError();
                GLES20.glBindTexture(GLES30.GL_TEXTURE_3D, lutTextureId);
                GLESUtil.checkGLError();
                GLES30.glTexImage3D(GLES30.GL_TEXTURE_3D, 0, GLES30.GL_RGB16F, currentCube.size, currentCube.size, currentCube.size, 0, GLES30.GL_RGB, GLES30.GL_FLOAT, byteBuffer);
                GLESUtil.checkGLError();
                GLES20.glBindTexture(GLES30.GL_TEXTURE_3D, 0);
                GLESUtil.checkGLError();
                lutSize = currentCube.size;
                lutEnable = true;
            }
        }else if (future == null && currentCube !=  null){
            currentCube = null;
            GLESUtil.delTextureId(lutTextureId);
            GLESUtil.checkGLError();
            lutTextureId = 0;
            lutSize = 0;
            lutEnable = false;
        }
        if (!lutEnable) {
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

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLESUtil.checkGLError();
        GLES20.glBindTexture(GLES30.GL_TEXTURE_3D, lutTextureId);
        GLESUtil.checkGLError();
        GLES20.glUniform1i(cubeLutTextureUniform, 1);
        GLESUtil.checkGLError();
        GLES20.glUniform1f(cubeLutSizeUniform, lutSize);
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
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES30.GL_TEXTURE_3D, 0);
        GLESUtil.checkGLError();
        success();
    }


    public void setCubeLutForAsset(String asset) {
        if (asset == null){
            lut3DFuture = null;
            return;
        }
        MessageHandler messageHandler = MessageHandler.obtain();
        lut3DFuture = messageHandler.submit(() -> CubeLut3D.createForAsset(asset));
        messageHandler.finishSafe();
    }
}
