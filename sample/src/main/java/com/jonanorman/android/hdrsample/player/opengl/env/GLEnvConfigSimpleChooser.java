package com.jonanorman.android.hdrsample.player.opengl.env;

public interface GLEnvConfigSimpleChooser extends GLEnvConfigChooser {

    void setAlphaSize(int alphaSize);

    void setBlueSize(int blueSize);

    void setGreenSize(int greenSize);

    void setRedSize(int redSize);

    void setDepthSize(int depthSize);

    void setStencilSize(int stencilSize);

    void setSamples(int samples);

    void setSampleBuffers(int sampleBuffers);

    void setRenderGL10(Boolean renderGL10);

    void setRenderGL20(Boolean renderGL20);

    void setRenderGL30(Boolean renderGL30);

    void setRecordable(Boolean recordable);


     static GLEnvConfigSimpleChooser createChooser() {
        return new EnvConfigSimpleChooserImpl();
    }
}
