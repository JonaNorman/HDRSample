package com.norman.android.hdrsample.todo;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import com.norman.android.hdrsample.player.AndroidTexturePlayer;
import com.norman.android.hdrsample.util.BufferUtil;
import com.norman.android.hdrsample.util.CubeLut3D;
import com.norman.android.hdrsample.util.GLESUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class CubeLutTextureRenderer extends AndroidTexturePlayer.TextureRenderer {

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
            "uniform mat4 textureMatrix;\n" +
            "void main() {\n" +
            "    gl_Position =position;\n" +
            "    textureCoordinate =(textureMatrix*inputTextureCoordinate).xy;\n" +
            "}";


    private static final String FRAGMENT_SHADER = "#version 300 es\n" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision highp float;\n" +
            "precision highp sampler3D;\n" +
            "in  vec2 textureCoordinate;\n" +
            "out vec4 outColor;\n" +
            "\n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            "uniform sampler3D cubeLutTexture;\n" +
            "uniform float cubeLutSize;\n" +
            "uniform bool cubeLutEnable;\n" +
            "\n" +
            "\n" +
            "void main() {\n" +
            "\n" +
            "    // Based on \\\\\\\\GPU Gems 2 — Chapter 24. Using Lookup Tables to Accelerate Color Transformations\\\\\\\\\n" +
            "    // More info and credits @ http://http.developer.nvidia.com/GPUGems2/gpugems2_chapter24.html\n" +
            "    vec4 rawColor = texture(inputImageTexture, textureCoordinate);\n" +
            "    if(cubeLutEnable) {\n" +
            "        // Compute the 3D LUT lookup scale/offset factor\n" +
            "        vec3 scale = vec3((cubeLutSize - 1.0) / cubeLutSize);\n" +
            "        vec3 offset = vec3(1.0 / (2.0 * cubeLutSize));\n" +
            "        outColor.rgb = texture(cubeLutTexture, scale * rawColor.rgb + offset).rgb;\n" +
            "    } else {\n" +
            "        outColor.rgb = rawColor.rgb;\n" +
            "    }\n" +
            "    outColor.a = rawColor.a;\n" +
            "}";


    private int lutTextureId;
    private int lutSize;

    private boolean lutEnable;

    private CubeLut3D pendCube;

    private CubeLut3D currentCube;

    private FloatBuffer textureCoordinateBuffer;
    private FloatBuffer positionCoordinateBuffer;

    private int positionCoordinateAttribute;
    private int textureCoordinateAttribute;
    private int textureUnitUniform;
    private int textureMatrixUniform;

    private int cubeLutTextureUniform;

    private int  cubeLutEnableUniform;

    private int cubeLutSizeUniform;

    private int programId;

    private boolean hasLocation;


    public CubeLutTextureRenderer() {
        positionCoordinateBuffer = BufferUtil.createDirectFloatBuffer(POSITION_COORDINATES);
        textureCoordinateBuffer = BufferUtil.createDirectFloatBuffer(TEXTURE_COORDINATES);
    }


    @Override
    protected void onCreate(AndroidTexturePlayer.TextureInfo textureInfo, AndroidTexturePlayer.SurfaceInfo surfaceInfo) {
        this.programId = GLESUtil.createProgramId(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    @Override
    protected void onClean() {
        GLESUtil.delProgramId(programId);
        GLESUtil.delTextureId(lutTextureId);
        currentCube = null;
        hasLocation = false;
        lutTextureId = 0;
        lutSize = 0;
        lutEnable = false;
    }

    @Override
    protected void onRender(AndroidTexturePlayer.TextureInfo textureInfo, AndroidTexturePlayer.SurfaceInfo surfaceInfo) {
        GLESUtil.checkGLError();
        if (pendCube != currentCube) {
            currentCube = pendCube;
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
                lutEnable =true;
            }
        }
        positionCoordinateBuffer.clear();
        textureCoordinateBuffer.clear();
        GLES20.glUseProgram(programId);
        GLESUtil.checkGLError();
        GLES20.glViewport(0, 0, surfaceInfo.width, surfaceInfo.height);
        GLESUtil.checkGLError();
        GLES30.glClearColor(0.0f, 0.f, 0.f, 1.0f);
        GLESUtil.checkGLError();
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLESUtil.checkGLError();
        if (!hasLocation) {
            hasLocation = true;
            positionCoordinateAttribute = GLES20.glGetAttribLocation(programId, "position");
            GLESUtil.checkGLError();
            textureCoordinateAttribute = GLES20.glGetAttribLocation(programId, "inputTextureCoordinate");
            GLESUtil.checkGLError();
            textureUnitUniform = GLES20.glGetUniformLocation(programId, "inputImageTexture");
            GLESUtil.checkGLError();
            textureMatrixUniform = GLES20.glGetUniformLocation(programId, "textureMatrix");
            GLESUtil.checkGLError();
            cubeLutTextureUniform = GLES20.glGetUniformLocation(programId, "cubeLutTexture");
            GLESUtil.checkGLError();
            cubeLutSizeUniform = GLES20.glGetUniformLocation(programId, "cubeLutSize");
            GLESUtil.checkGLError();
            cubeLutEnableUniform = GLES20.glGetUniformLocation(programId, "cubeLutEnable");
            GLESUtil.checkGLError();
        }
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
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureInfo.textureId);
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
        GLES30.glUniform1i(cubeLutEnableUniform, lutEnable?1:0);
        GLESUtil.checkGLError();
        GLES20.glUniformMatrix4fv(textureMatrixUniform, 1, false, textureInfo.textureMatrix.get(), 0);
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
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES30.GL_TEXTURE_3D, 0);
        GLESUtil.checkGLError();
    }


    public void setCubeLutForAsset(String asset) {
        long time = System.currentTimeMillis();
        pendCube = CubeLut3D.createForAsset(asset);
        Log.v("111111",(System.currentTimeMillis() - time)+"ms");
    }
}
