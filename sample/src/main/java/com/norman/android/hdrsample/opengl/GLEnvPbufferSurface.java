package com.norman.android.hdrsample.opengl;


public interface GLEnvPbufferSurface extends GLEnvSurface {

     static GLEnvPbufferSurface create(GLEnvContext envContext, int width, int height){
         return new Builder(envContext, width, height)
                 .build();
     }

    class Builder {
        GLEnvDisplay envDisplay;
        GLEnvConfig envConfig;

        // surface属性

        EnvPbufferSurfaceAttribArrayImpl surfaceAttrib = new EnvPbufferSurfaceAttribArrayImpl();


        public Builder(GLEnvContext envContext, int width, int height) {
            this(envContext.getEnvDisplay(), envContext.getEnvConfig(), width, height);
        }


        public Builder(GLEnvDisplay envDisplay, GLEnvConfig envConfig, int width, int height) {
            this.envDisplay = envDisplay;
            this.envConfig = envConfig;
            setWidth(width);
            setHeight(height);

        }

        public Builder(GLEnvDisplay envDisplay, GLEnvConfigChooser envConfigChooser, int width, int height) {
            this(envDisplay, envDisplay.chooseConfig(envConfigChooser), width, height);
        }

        public void setWidth(int width) {
            surfaceAttrib.setWidth(width);
        }

        public void setHeight(int height) {
            surfaceAttrib.setHeight(height);
        }

        /**
         * 设置色域
         * @param colorSpace
         */
        public void setColorSpace(@EGLColorSpace int colorSpace) {
            surfaceAttrib.setColorSpace(colorSpace);
        }

        public void setSurfaceAttrib(int key, int value) {
            surfaceAttrib.setAttrib(key, value);
        }
        public GLEnvPbufferSurface build() {
            return new EnvPbufferSurfaceImpl(envDisplay, envConfig, surfaceAttrib.clone());
        }

    }

}
