package com.norman.android.hdrsample.player;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import androidx.annotation.CallSuper;

import com.norman.android.hdrsample.opengl.GLMatrix;
import com.norman.android.hdrsample.player.shader.TextureFragmentShader;
import com.norman.android.hdrsample.player.shader.TextureVertexShader;
import com.norman.android.hdrsample.util.GLESUtil;

import java.nio.FloatBuffer;

/**
 * 支持3种格式 2D OES Y2Y渲染到frameBuffer上
 */
class GLTextureRenderer extends GLRenderer {


    private final float[] textureMatrix = new GLMatrix().get();

    private final FloatBuffer textureCoordinateBuffer;
    private final FloatBuffer positionCoordinateBuffer;

    private int positionCoordinateAttribute;
    private int textureCoordinateAttribute;
    private int textureUnitUniform;
    private int textureMatrixUniform;


    private int textureId;

    private final @TextureFragmentShader.TextureType int textureType;


    public GLTextureRenderer(@TextureFragmentShader.TextureType int type) {
        textureType = type;
        positionCoordinateBuffer = GLESUtil.createPositionFlatBuffer();//平面的顶点坐标
        textureCoordinateBuffer = GLESUtil.createTextureFlatBuffer();//纹理坐标
        setVertexShader(new TextureVertexShader());
        setFrameShader(new TextureFragmentShader(textureType));
    }


    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }


    public float[] getTextureMatrix() {
        return textureMatrix;
    }

    @CallSuper
    @Override
    protected void onProgramChange(int programId) {
        positionCoordinateAttribute = GLES20.glGetAttribLocation(programId, TextureVertexShader.POSITION);
        textureMatrixUniform = GLES20.glGetUniformLocation(programId, TextureVertexShader.TEXTURE_MATRIX);
        textureCoordinateAttribute = GLES20.glGetAttribLocation(programId, TextureVertexShader.INPUT_TEXTURE_COORDINATE);
        textureUnitUniform = GLES20.glGetUniformLocation(programId, TextureFragmentShader.INPUT_IMAGE_TEXTURE);
    }

    @Override
    protected boolean onRenderStart() {
        return textureId > 0;
    }

    @Override
    protected void onRender() {

        positionCoordinateBuffer.clear();
        textureCoordinateBuffer.clear();
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
        if (textureType != TextureFragmentShader.TYPE_TEXTURE_2D) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        }
    }

    protected void onTextureRender() {

    }

}
