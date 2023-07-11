package com.norman.android.hdrsample.opengl;


public interface GLEnvPbufferSurface extends GLEnvSurface {

     static GLEnvPbufferSurface create(GLEnvContext envContext, int width, int height){
         return new Builder(envContext, width, height)
                 .build();
     }

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

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setColorSpace(@ColorSpace int colorSpace) {
            this.colorSpace = colorSpace;
        }

        public GLEnvPbufferSurface build() {
            GLEnvPbufferSurfaceAttribArray surfaceAttrib = new EnvPbufferSurfaceAttribArrayImpl();
            surfaceAttrib.setWidth(width);
            surfaceAttrib.setHeight(height);
            if (colorSpace != null) {
                surfaceAttrib.setColorSpace(colorSpace);
            }
            return new EnvPbufferSurfaceImpl(envDisplay, envConfig, surfaceAttrib);
        }

    }

}
