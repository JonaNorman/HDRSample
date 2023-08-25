package com.norman.android.hdrsample.opengl;

/**
 * EGL的config配置选择器简单实现，注意选取出来的配置大于等于要求的配置，譬如选取的RGBA8888不支持，出来的可能就是RGBA16161616
 */
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
        //默认要求支持OpenGL2.0和3.0
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


        /**
         * 是否支持OpenGL1.0
         * @param renderGL10 null 表示都可以， true表示要支持，false表示不支持
         */
        public void setRenderGL10(Boolean renderGL10) {
            this.renderGL10 = renderGL10;
        }

        /**
         * 是否支持OpenGL2.0
         * @param renderGL20 null 表示都可以， true表示要支持，false表示不支持
         */
        public void setRenderGL20(Boolean renderGL20) {
            this.renderGL20 = renderGL20;
        }

        /**
         * 是否支持OpenGL3.0
         * @param renderGL30 null 表示都可以， true表示要支持，false表示不支持
         */
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
