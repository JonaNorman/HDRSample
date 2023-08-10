package com.norman.android.hdrsample.opengl;


import android.view.Surface;

public interface GLEnvWindowSurface extends GLEnvSurface {


     static GLEnvWindowSurface create(GLEnvContext envContext, Surface surface){
        GLEnvWindowSurface.Builder builder = new GLEnvWindowSurface.Builder(envContext, surface);
        return   builder.build();
    }
    void setPresentationTime(long presentationNs);

    void swapBuffers();

    Surface getSurface();

    boolean isValid();


    class Builder {
        GLEnvDisplay envDisplay;
        GLEnvConfig envConfig;
        Surface surface;
        EnvWindowSurfaceAttribImpl windowSurfaceAttrib = new EnvWindowSurfaceAttribImpl();


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

        public void setColorSpace(@ColorSpace int colorSpace) {
            windowSurfaceAttrib.setColorSpace(colorSpace);
        }

        public void setSurfaceAttrib(int key, int value) {
            windowSurfaceAttrib.setAttrib(key, value);
        }

        public GLEnvWindowSurface build() {
            return new EnvWindowSurfaceImpl(envDisplay, envConfig, surface, windowSurfaceAttrib.clone());
        }

    }
}
