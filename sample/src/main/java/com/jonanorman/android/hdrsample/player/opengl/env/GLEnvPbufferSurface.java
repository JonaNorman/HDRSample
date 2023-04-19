package com.jonanorman.android.hdrsample.player.opengl.env;


public interface GLEnvPbufferSurface extends GLEnvSurface {

    class Builder {
        GLEnvDisplay envDisplay;
        GLEnvConfig envConfig;

        int width;
        int height;

        Integer colorSpace;


        public Builder(GLEnvContext envContext, int width, int height) {
            this(envContext.getEnvDisplay(), envContext.getEnvConfig(), width, height);
        }


        public Builder(GLEnvDisplay envDisplay, GLEnvConfig envConfig, int width, int height) {
            this.envDisplay = envDisplay;
            this.envConfig = envConfig;
            this.width = width;
            this.height = height;
        }

        public Builder(GLEnvDisplay envDisplay, GLEnvConfigChooser envConfigChooser, int width, int height) {
            this(envDisplay, envDisplay.chooseConfig(envConfigChooser), width, height);
        }


        public GLEnvPbufferSurface build() {
            GLEnvPbufferSurfaceAttrib surfaceAttrib = new EnvPbufferSurfaceAttribImpl();
            surfaceAttrib.setWidth(width);
            surfaceAttrib.setHeight(height);
            if (colorSpace != null) {
                surfaceAttrib.setColorSpace(colorSpace);
            }
            return new EnvPbufferSurfaceImpl(envDisplay, envConfig, surfaceAttrib);
        }


        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setColorSpace(@ColorSpace int colorSpace) {
            this.colorSpace = colorSpace;
        }
    }

}
