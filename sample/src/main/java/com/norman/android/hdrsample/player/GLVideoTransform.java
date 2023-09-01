package com.norman.android.hdrsample.player;

/**
 * 视频图像处理，如果需要输出到新的纹理需要调用success
 */
public abstract class GLVideoTransform extends GLRenderer {

    GLRenderTextureTarget inputTarget;
    GLRenderTextureTarget outputTarget;


    protected final int getInputWidth() {
        return inputTarget.renderWidth;
    }

    protected final int getInputHeight() {
        return inputTarget.renderHeight;
    }

    protected final int getInputBitDepth() {
        return inputTarget.bitDepth;
    }


    protected final int getInputTextureId() {
        return inputTarget.textureId;
    }


    protected final @VideoOutput.ColorSpace int getInputColorSpace() {
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
        return outputTarget.renderWidth;
    }

    protected final int getOutputHeight() {
        return outputTarget.renderHeight;
    }


    protected final void clearColor() {
        outputTarget.clearColor();
    }


    void renderToTarget(GLRenderTextureTarget inputTarget, GLRenderTextureTarget outputTarget) {
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

    protected final void setOutputColorSpace(@VideoOutput.ColorSpace int colorSpace) {
        outputTarget.setColorSpace(colorSpace);
        if (colorSpace == VideoOutput.ColorSpace.VIDEO_SDR) {
            outputTarget.setMaxContentLuminance(0);
            outputTarget.setMaxFrameAverageLuminance(0);
            outputTarget.setMaxMasteringLuminance(0);
        }
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
