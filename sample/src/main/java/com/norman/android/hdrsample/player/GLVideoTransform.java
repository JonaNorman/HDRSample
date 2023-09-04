package com.norman.android.hdrsample.player;

import com.norman.android.hdrsample.player.color.ColorSpace;

/**
 * 视频图像处理，如果需要输出到新的纹理需要调用success
 */
public abstract class GLVideoTransform extends GLRenderer {

    GLRenderTextureTarget inputTarget;
    GLRenderTextureTarget outputTarget;

    boolean enable;


    protected final int getInputWidth() {
        return inputTarget.width;
    }

    protected final int getInputHeight() {
        return inputTarget.height;
    }

    protected final int getInputBitDepth() {
        return inputTarget.bitDepth;
    }


    protected final int getInputTextureId() {
        return inputTarget.textureId;
    }


    protected final @ColorSpace int getInputColorSpace() {
        return inputTarget.colorSpace;
    }

    /**
     * @return
     */
    protected final int getInputMaxContentLuminance() {
        return inputTarget.maxContentLuminance;
    }

    /**
     * @return
     */
    protected final int getInputMaxFrameAverageLuminance() {
        return inputTarget.maxFrameAverageLuminance;
    }


    /**
     * @return
     */
    protected final int getInputMaxMasteringLuminance() {
        return inputTarget.maxMasteringLuminance;
    }


    protected final int getOutputWidth() {
        return outputTarget.width;
    }

    protected final int getOutputHeight() {
        return outputTarget.height;
    }


    protected final void clearColor() {
        outputTarget.clearColor();
    }


    synchronized void renderToTarget(GLRenderTextureTarget inputTarget, GLRenderTextureTarget outputTarget) {
        if (!enable){
            return;
        }
        this.inputTarget = inputTarget;
        this.outputTarget = outputTarget;
        outputTarget.setColorSpace(inputTarget.colorSpace);
        outputTarget.setMaxContentLuminance(inputTarget.maxContentLuminance);
        outputTarget.setMaxFrameAverageLuminance(inputTarget.maxFrameAverageLuminance);
        outputTarget.setMaxMasteringLuminance(inputTarget.maxMasteringLuminance);
        outputTarget.setBitDepth(inputTarget.bitDepth);
        super.renderToTarget(outputTarget);
    }

    @Override
    synchronized void renderToTarget(GLRenderTarget renderTarget) {
        throw new RuntimeException("not support renderToTarget(GLRenderTarget renderTarget)");
    }

    protected final void setOutputColorSpace(@ColorSpace int colorSpace) {
        outputTarget.setColorSpace(colorSpace);
        if (colorSpace == ColorSpace.VIDEO_SDR) {
            outputTarget.setMaxContentLuminance(0);
            outputTarget.setMaxFrameAverageLuminance(0);
            outputTarget.setMaxMasteringLuminance(0);
        }
    }

    public synchronized void enable(){
        enable = true;
    }

    public synchronized void disable(){
        enable = false;
    }


    @Override
    final boolean onRenderStart() {
        return onTransformStart();
    }


    @Override
    final void onRender() {
        onTransform();
    }

    @Override
    void onRenderFinish() {
        onTransformFinish();
    }

    protected abstract boolean onTransformStart();

    protected abstract void onTransform();

    protected  void onTransformFinish(){

    }
}
