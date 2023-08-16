package com.norman.android.hdrsample.player;

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
            outTextureTarget.setBitDepth(inputTarget.bitDepth);
        }
        super.renderToTarget(renderTarget);
    }

    protected final void success(){
        transformSuccess = true;
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
