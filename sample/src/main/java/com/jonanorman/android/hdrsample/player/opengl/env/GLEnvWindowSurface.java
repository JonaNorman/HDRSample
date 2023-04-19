package com.jonanorman.android.hdrsample.player.opengl.env;


import android.view.Surface;

public interface GLEnvWindowSurface extends GLEnvSurface {


    void setPresentationTime(long presentationNs);

    void swapBuffers();

    Surface getSurface();


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
            GLEnvWindowSurfaceAttrib windowSurfaceAttrib = new EnvWindowSurfaceAttribImpl();
            if (colorSpace != null) {
                windowSurfaceAttrib.setColorSpace(colorSpace);
            }
            return new EnvWindowSurfaceImpl(envDisplay, envConfig, surface, windowSurfaceAttrib);
        }

    }
}
