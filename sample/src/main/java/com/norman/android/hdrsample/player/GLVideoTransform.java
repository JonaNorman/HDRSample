package com.norman.android.hdrsample.player;

/**
 * 视频图像处理，如果需要输出到新的纹理需要调用success
 */
public abstract class GLVideoTransform  extends GLRenderer {

    GLRenderTextureTarget inputTarget;

    boolean  transformSuccess;


    protected  final int getInputWidth(){
        return inputTarget.renderWidth;
    }

    protected  final int getInputHeight(){
        return inputTarget.renderHeight;
    }

    protected  final int getInputBitDepth(){
        return inputTarget.bitDepth;
    }


    protected  final int getInputTextureId(){
        return inputTarget.textureId;
    }


    protected final @VideoOutput.ColorSpace int getInputColorSpace() {
        return inputTarget.colorSpace;
    }

    /**
     * HDR的MaxContentLuminance在HDR转SDR有用
     * @return
     */
    protected final  int getInputMaxContentLuminance() {
        return inputTarget.maxContentLuminance;
    }


    protected  final int getOutputWidth(){
        return outputTarget.renderWidth;
    }

    protected  final int getOutputHeight(){
        return outputTarget.renderHeight;
    }



    protected final void clearColor(){
         outputTarget.clearColor();
    }


    void renderToTarget(GLRenderTextureTarget inputTarget,GLRenderTarget renderTarget) {
        this.inputTarget = inputTarget;
        if (renderTarget instanceof GLRenderTextureTarget){
            GLRenderTextureTarget outTextureTarget  = (GLRenderTextureTarget) renderTarget;
            outTextureTarget.setColorSpace(inputTarget.colorSpace);
            outTextureTarget.setMaxContentLuminance(inputTarget.maxContentLuminance);
            outTextureTarget.setBitDepth(inputTarget.bitDepth);
        }
        super.renderToTarget(renderTarget);
    }

    protected final void success(){
        transformSuccess = true;
    }

    protected final void success(@VideoOutput.ColorSpace int colorSpace,int maxContentLuminance){
        transformSuccess = true;
        if (outputTarget instanceof GLRenderTextureTarget){
            GLRenderTextureTarget outTextureTarget = (GLRenderTextureTarget) outputTarget;
            outTextureTarget.setColorSpace(colorSpace);
            outTextureTarget.setMaxContentLuminance(maxContentLuminance);
        }
    }
    protected final void success(@VideoOutput.ColorSpace int colorSpace){
        transformSuccess = true;
        if (outputTarget instanceof GLRenderTextureTarget){
            GLRenderTextureTarget outTextureTarget = (GLRenderTextureTarget) outputTarget;
            outTextureTarget.setColorSpace(colorSpace);
        }
    }


    @Override
    void onRender() {
        transform();
    }



    void transform(){
        transformSuccess = false;
        onTransform();
    }

    protected abstract void onTransform();
}
