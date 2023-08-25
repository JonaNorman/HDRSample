package com.norman.android.hdrsample.opengl;


import android.view.Surface;

/**
 * EGLWindowSurface的封装
 */
public interface GLEnvWindowSurface extends GLEnvSurface {


    /**
     * 注意一个Surface只能同时和一个EGLWindowSurface绑定
     * @param envContext
     * @param surface
     * @return
     */
     static GLEnvWindowSurface create(GLEnvContext envContext, Surface surface){
        GLEnvWindowSurface.Builder builder = new GLEnvWindowSurface.Builder(envContext, surface);
        return   builder.build();
    }

    /**
     * 设置Surface的时间
     * @param presentationNs
     */
    void setPresentationTime(long presentationNs);

    void swapBuffers();

    Surface getSurface();

    boolean isValid();


    class Builder {
        GLEnvDisplay envDisplay;
        GLEnvConfig envConfig;
        Surface surface;
        EnvWindowSurfaceImpl.AttribImpl windowSurfaceAttrib = new EnvWindowSurfaceImpl.AttribImpl();


        public Builder(GLEnvContext envContext, Surface surface) {
            this(envContext.getEnvDisplay(), envContext.getEnvConfig(), surface);
        }

        public Builder(GLEnvContext envContext,GLEnvConfig envConfig, Surface surface) {
            this(envContext.getEnvDisplay(), envConfig, surface);
        }


        public Builder(GLEnvDisplay envDisplay, GLEnvConfig envConfig, Surface surface) {
            this.envDisplay = envDisplay;
            this.envConfig = envConfig;
            this.surface = surface;
        }

        public Builder(GLEnvDisplay envDisplay, GLEnvConfigChooser envConfigChooser, Surface surface) {
            this(envDisplay, envDisplay.chooseConfig(envConfigChooser), surface);
        }

        /**
         * 设置色域
         * @param colorSpace
         */
        public void setColorSpace(@EGLColorSpace int colorSpace) {
            windowSurfaceAttrib.setColorSpace(colorSpace);
        }

        public void setSurfaceAttrib(int key, int value) {
            windowSurfaceAttrib.setAttrib(key, value);
        }

        public GLEnvWindowSurface build() {
            return new EnvWindowSurfaceImpl(envDisplay, envConfig, surface, windowSurfaceAttrib.clone());
        }

    }

    interface AttrList extends GLEnvSurface.AttrList {

    }
}
