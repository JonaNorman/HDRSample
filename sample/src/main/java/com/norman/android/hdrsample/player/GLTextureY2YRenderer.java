package com.norman.android.hdrsample.player;

import android.opengl.GLES20;

import com.norman.android.hdrsample.player.shader.TextureFragmentShader;
import com.norman.android.hdrsample.util.ColorMatrixUtil;

class GLTextureY2YRenderer extends GLTextureRenderer {

    private static final String EXTENSION_YUV_TARGET = "GL_EXT_YUV_target";

    private int yuvToRgbMatrixUniform;

    private @ColorMatrixUtil.ColorRange int colorRange = ColorMatrixUtil.COLOR_RANGE_LIMITED;


    private int bitDepth = 8;


    public GLTextureY2YRenderer() {
        super(TextureFragmentShader.TYPE_TEXTURE_Y2Y);
    }


    public void setColorRange(int colorRange) {
        this.colorRange = colorRange;
    }

    public void setBitDepth(int bitDepth) {
        this.bitDepth = bitDepth;
    }


    @Override
    protected void onProgramChange(int programId) {
        super.onProgramChange(programId);
        yuvToRgbMatrixUniform = GLES20.glGetUniformLocation(programId, TextureFragmentShader.Y2Y_TO_RGB_MATRIX);
    }


    @Override
    protected void onTextureRender() {
        super.onTextureRender();
        GLES20.glUniformMatrix4fv(yuvToRgbMatrixUniform, 1, false, ColorMatrixUtil.getYuvToRgbMatrix(bitDepth, colorRange), 0);
    }

    /**
     * 是否支持Y2Y扩展支持
     *
     * @return
     */
    public static boolean isSupportY2YEXT() {
        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        return extensions.contains(EXTENSION_YUV_TARGET);
    }
}
