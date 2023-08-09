package com.norman.android.hdrsample.opengl;

public interface GLEnvConfigSimpleChooser extends GLEnvConfigChooser {


    static GLEnvConfigSimpleChooser create() {
        return new GLEnvConfigSimpleChooser.Builder().build();
    }

    class Builder {

        int alphaSize = 8;
        int blueSize = 8;
        int greenSize = 8;
        int redSize = 8;
        int depthSize;
        int stencilSize;
        int samples;
        int sampleBuffers;
        boolean windowSurface = true;
        boolean pbufferSurface = true;
        Boolean renderGL10;
        Boolean renderGL20 = true;
        Boolean renderGL30 = true;
        Boolean recordable;


        public void setAlphaSize(int alphaSize) {
            this.alphaSize = alphaSize;
        }


        public void setBlueSize(int blueSize) {
            this.blueSize = blueSize;
        }


        public void setGreenSize(int greenSize) {
            this.greenSize = greenSize;
        }


        public void setRedSize(int redSize) {
            this.redSize = redSize;
        }


        public void setDepthSize(int depthSize) {
            this.depthSize = depthSize;
        }


        public void setStencilSize(int stencilSize) {
            this.stencilSize = stencilSize;
        }


        public void setSamples(int samples) {
            this.samples = samples;
        }


        public void setSampleBuffers(int sampleBuffers) {
            this.sampleBuffers = sampleBuffers;
        }


        public void setRenderGL10(Boolean renderGL10) {
            this.renderGL10 = renderGL10;
        }


        public void setRenderGL20(Boolean renderGL20) {
            this.renderGL20 = renderGL20;
        }


        public void setRenderGL30(Boolean renderGL30) {
            this.renderGL30 = renderGL30;
        }

        public void setRecordable(Boolean recordable) {
            this.recordable = recordable;
        }

        public GLEnvConfigSimpleChooser build() {
            return new EnvConfigSimpleChooserImpl(this);
        }
    }
}
