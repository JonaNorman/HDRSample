package com.norman.android.hdrsample.player;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import androidx.annotation.CallSuper;

import com.norman.android.hdrsample.opengl.GLMatrix;
import com.norman.android.hdrsample.player.shader.TextureFragmentShader;
import com.norman.android.hdrsample.player.shader.TextureVertexShader;
import com.norman.android.hdrsample.util.GLESUtil;

import java.nio.FloatBuffer;

class GLTextureRenderer extends GLRenderer {


    private final float[] textureMatrix = new GLMatrix().get();

    private final FloatBuffer textureCoordinateBuffer;
    private final FloatBuffer positionCoordinateBuffer;

    private int positionCoordinateAttribute;
    private int textureCoordinateAttribute;
    private int textureUnitUniform;
    private int textureMatrixUniform;

    private int programId;


    private int textureId;

    private final @TextureFragmentShader.TextureType int textureType;
    private TextureFragmentShader fragmentShader;
    private TextureVertexShader vertexShader;




    public GLTextureRenderer(@TextureFragmentShader.TextureType int type) {
        textureType = type;
        positionCoordinateBuffer = GLESUtil.createPositionFlatBuffer();//平面的顶点坐标
        textureCoordinateBuffer = GLESUtil.createTextureFlatBuffer();//纹理坐标
    }

    @Override
    public void onCreate() {
        vertexShader = new TextureVertexShader();
        fragmentShader = new TextureFragmentShader(textureType);
        programId = GLESUtil.createProgramId(vertexShader.getCode(), fragmentShader.getCode());
        onProgramChange(programId);
    }


    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }


    public float[] getTextureMatrix() {
        return textureMatrix;
    }


    @CallSuper
    protected void onProgramChange(int programId) {
        positionCoordinateAttribute = GLES20.glGetAttribLocation(programId, TextureVertexShader.POSITION);
        textureMatrixUniform = GLES20.glGetUniformLocation(programId, TextureVertexShader.TEXTURE_MATRIX);
        textureCoordinateAttribute = GLES20.glGetAttribLocation(programId, TextureVertexShader.INPUT_TEXTURE_COORDINATE);
        textureUnitUniform = GLES20.glGetUniformLocation(programId, TextureFragmentShader.INPUT_IMAGE_TEXTURE);
    }


    @Override
    final void onRender() {
        if (textureId == 0) return;
        positionCoordinateBuffer.clear();
        textureCoordinateBuffer.clear();
        GLES20.glUseProgram(programId);
        GLES20.glEnableVertexAttribArray(positionCoordinateAttribute);
        GLES20.glVertexAttribPointer(positionCoordinateAttribute,
                GLESUtil.FLAT_VERTEX_LENGTH,
                GLES20.GL_FLOAT, false, 0,
                positionCoordinateBuffer);
        GLES20.glEnableVertexAttribArray(textureCoordinateAttribute);
        GLES20.glVertexAttribPointer(textureCoordinateAttribute,
                GLESUtil.FLAT_VERTEX_LENGTH,
                GLES20.GL_FLOAT, false, 0,
                textureCoordinateBuffer);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        if (textureType != TextureFragmentShader.TYPE_TEXTURE_2D) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        }
        GLES20.glUniform1i(textureUnitUniform, 0);
        GLES20.glUniformMatrix4fv(textureMatrixUniform, 1, false, textureMatrix, 0);
        onTextureRender();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(positionCoordinateAttribute);
        GLES20.glDisableVertexAttribArray(textureCoordinateAttribute);
        GLES20.glUseProgram(0);
        if (textureType != TextureFragmentShader.TYPE_TEXTURE_2D) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        }
    }

    protected void onTextureRender() {

    }

}
