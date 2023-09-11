package com.norman.android.hdrsample.transform;

import android.opengl.GLES20;

import androidx.annotation.NonNull;

import com.norman.android.hdrsample.player.color.ColorSpace;
import com.norman.android.hdrsample.player.GLVideoTransform;
import com.norman.android.hdrsample.transform.shader.HDRToSDRShader;
import com.norman.android.hdrsample.transform.shader.MetaDataParams;
import com.norman.android.hdrsample.transform.shader.chromacorrect.ChromaCorrection;
import com.norman.android.hdrsample.transform.shader.gamma.GammaOETF;
import com.norman.android.hdrsample.transform.shader.gamutmap.GamutMap;
import com.norman.android.hdrsample.transform.shader.tonemap.ToneMap;
import com.norman.android.hdrsample.util.DisplayUtil;
import com.norman.android.hdrsample.util.GLESUtil;

import java.nio.FloatBuffer;

public class HDRToSDRVideoTransform extends GLVideoTransform {

    public static final int TONE_DISPLAY_REFERENCE = 1;

    public static final int TONE_SCENE_REFERENCE = 2;

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

    private HDRToSDRShader hdrToSDRShader;

    private ScreenBrightnessObserver screenBrightnessObserver;
    private int maxDisplayLuminanceUniform;
    private int currentDisplayLuminanceUniform;
    private int hdrPeakLuminanceUniform;

    private ChromaCorrection chromaCorrection = ChromaCorrection.NONE;
    private ToneMap toneMap = ToneMap.NONE;
    private GamutMap gamutMap = GamutMap.NONE;
    private GammaOETF gammaOETF = GammaOETF.NONE;

    private boolean shaderChange;

    private int toneReference = TONE_DISPLAY_REFERENCE;


    public HDRToSDRVideoTransform() {
        positionCoordinateBuffer = GLESUtil.createPositionFlatBuffer();
        textureCoordinateBuffer = GLESUtil.createTextureFlatBuffer();
        screenBrightnessObserver = new ScreenBrightnessObserver();
        setVertexShader(VERTEX_SHADER);
    }

    @Override
    protected void onCreate() {
        screenBrightnessObserver.listen();
    }

    @Override
    protected boolean onTransformStart() {
        if (chromaCorrection == null ||
                toneMap == null ||
                gamutMap == null ||
                gammaOETF == null) {
            return false;
        }

        if (chromaCorrection == ChromaCorrection.NONE &&
                toneMap == ToneMap.NONE &&
                gamutMap == GamutMap.NONE &&
                gammaOETF == GammaOETF.NONE) {
            return false;
        }
        int colorSpace = getInputColorSpace();
        if (colorSpace == ColorSpace.VIDEO_SDR) {
            return false;
        }
        if (shaderChange || (hdrToSDRShader != null &&
                hdrToSDRShader.colorSpace != colorSpace)) {
            hdrToSDRShader = new HDRToSDRShader(colorSpace,
                    chromaCorrection,
                    toneMap,
                    gamutMap,
                    gammaOETF,
                    toneReference == TONE_DISPLAY_REFERENCE

            );
            setFrameShader(hdrToSDRShader);
            shaderChange  =false;
        }
        return true;
    }

    @Override
    protected void onProgramChange(int programId) {
        positionCoordinateAttribute = GLES20.glGetAttribLocation(programId, "position");
        textureCoordinateAttribute = GLES20.glGetAttribLocation(programId, "inputTextureCoordinate");
        textureUnitUniform = GLES20.glGetUniformLocation(programId, "inputImageTexture");
        maxDisplayLuminanceUniform = GLES20.glGetUniformLocation(programId, MetaDataParams.MAX_DISPLAY_LUMINANCE);
        currentDisplayLuminanceUniform = GLES20.glGetUniformLocation(programId, MetaDataParams.CURRENT_DISPLAY_LUMINANCE);
        hdrPeakLuminanceUniform = GLES20.glGetUniformLocation(programId, MetaDataParams.HDR_PEAK_LUMINANCE);
    }

    @Override
    protected synchronized void onTransform() {
        setOutputColorSpace(ColorSpace.VIDEO_SDR);
        clearColor();
        positionCoordinateBuffer.clear();
        textureCoordinateBuffer.clear();
        GLES20.glEnableVertexAttribArray(positionCoordinateAttribute);
        GLES20.glVertexAttribPointer(positionCoordinateAttribute, VERTEX_LENGTH, GLES20.GL_FLOAT, false, 0, positionCoordinateBuffer);
        GLES20.glEnableVertexAttribArray(textureCoordinateAttribute);
        GLES20.glVertexAttribPointer(textureCoordinateAttribute, VERTEX_LENGTH, GLES20.GL_FLOAT, false, 0, textureCoordinateBuffer);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getInputTextureId());
        GLES20.glUniform1i(textureUnitUniform, 0);
        GLES20.glUniform1f(maxDisplayLuminanceUniform, DisplayUtil.getMaxLuminance());
        int peakLuminance = Math.min(getInputMaxContentLuminance(), getInputMaxMasteringLuminance());
        peakLuminance = Math.max(peakLuminance, getInputMaxFrameAverageLuminance());
        if (peakLuminance == 0) {
            peakLuminance = 1000;
        }
        GLES20.glUniform1f(hdrPeakLuminanceUniform, peakLuminance);
        GLES20.glUniform1f(currentDisplayLuminanceUniform, screenBrightnessObserver.getBrightnessInfo().brightnessFloat * DisplayUtil.getMaxLuminance());
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(positionCoordinateAttribute);
        GLES20.glDisableVertexAttribArray(textureCoordinateAttribute);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }


    public synchronized void setChromaCorrection(@NonNull ChromaCorrection chromaCorrection) {
        if (this.chromaCorrection != chromaCorrection) {
            this.chromaCorrection = chromaCorrection;
            shaderChange = true;
        }
    }

    public synchronized void setToneMap(@NonNull ToneMap toneMap) {
        if (this.toneMap != toneMap) {
            this.toneMap = toneMap;
            shaderChange = true;
        }
    }

    public synchronized void setGamutMap(@NonNull GamutMap gamutMap) {
        if (this.gamutMap != gamutMap) {
            this.gamutMap = gamutMap;
            shaderChange = true;
        }
    }

    public synchronized void setGammaOETF(@NonNull GammaOETF gammaOETF) {
        if (this.gammaOETF != gammaOETF) {
            this.gammaOETF = gammaOETF;
            shaderChange = true;
        }
    }

    public synchronized void setToneReference(int type){
        if (this.toneReference != type){
            this.toneReference = type;
            shaderChange = true;
        }
    }

    public synchronized ChromaCorrection getChromaCorrection() {
        return chromaCorrection;
    }

    public synchronized ToneMap getToneMap() {
        return toneMap;
    }

    public synchronized GamutMap getGamutMap() {
        return gamutMap;
    }

    public synchronized GammaOETF getGammaOETF() {
        return gammaOETF;
    }

    public int getToneReference() {
        return toneReference;
    }
}
