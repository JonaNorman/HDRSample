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

        Integer colorSpace;


        public Builder(GLEnvContext envContext, Surface surface) {
            this(envContext.getEnvDisplay(), envContext.getEnvConfig(), surface);
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
            this.colorSpace = colorSpace;
        }


        public GLEnvWindowSurface build() {
            GLEnvWindowSurfaceAttribArray windowSurfaceAttrib = new EnvWindowSurfaceAttribImpl();
            if (colorSpace != null) {
                windowSurfaceAttrib.setColorSpace(colorSpace);
            }
            return new EnvWindowSurfaceImpl(envDisplay, envConfig, surface, windowSurfaceAttrib);
        }

    }
}
