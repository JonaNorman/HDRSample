package com.norman.android.hdrsample.player.opengl;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;


import java.util.ArrayList;
import java.util.List;

class EnvConfigImpl implements GLEnvConfig {


    List<ConfigValue> configValueList = new ArrayList<>();
    ConfigValue bufferSize = new ConfigValue(EGL14.EGL_BUFFER_SIZE);
    ConfigValue alphaSize = new ConfigValue(EGL14.EGL_ALPHA_SIZE);
    ConfigValue blueSize = new ConfigValue(EGL14.EGL_BLUE_SIZE);
    ConfigValue greenSize = new ConfigValue(EGL14.EGL_GREEN_SIZE);
    ConfigValue redSize = new ConfigValue(EGL14.EGL_RED_SIZE);
    ConfigValue depthSize = new ConfigValue(EGL14.EGL_DEPTH_SIZE);
    ConfigValue stencilSize = new ConfigValue(EGL14.EGL_STENCIL_SIZE);
    ConfigBoolValue slowCaveat = new ConfigBoolValue(EGL14.EGL_CONFIG_CAVEAT, EGL14.EGL_SLOW_CONFIG);
    ConfigValue configId = new ConfigValue(EGL14.EGL_CONFIG_ID);
    ConfigValue level = new ConfigValue(EGL14.EGL_LEVEL);
    ConfigValue maxPbufferHeight = new ConfigValue(EGL14.EGL_MAX_PBUFFER_HEIGHT);
    ConfigValue maxPbufferPixels = new ConfigValue(EGL14.EGL_MAX_PBUFFER_PIXELS);
    ConfigValue maxPbufferWidth = new ConfigValue(EGL14.EGL_MAX_PBUFFER_WIDTH);
    ConfigBoolValue nativeRenderable = new ConfigBoolValue(EGL14.EGL_NATIVE_RENDERABLE, EGL14.EGL_TRUE);
    ConfigValue nativeVisualId = new ConfigValue(EGL14.EGL_NATIVE_VISUAL_ID);
    ConfigValue nativeVisualType = new ConfigValue(EGL14.EGL_NATIVE_VISUAL_TYPE);
    ConfigValue samples = new ConfigValue(EGL14.EGL_SAMPLES);
    ConfigValue sampleBuffers = new ConfigValue(EGL14.EGL_SAMPLE_BUFFERS);
    ConfigBoolValue windowSurfaceType = new ConfigBoolValue(EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT);
    ConfigBoolValue pbufferSurfaceType = new ConfigBoolValue(EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT);
    ConfigBoolValue transparentType = new ConfigBoolValue(EGL14.EGL_TRANSPARENT_TYPE, EGL14.EGL_TRANSPARENT_RGB);
    ConfigValue transparentRedValue = new ConfigValue(EGL14.EGL_TRANSPARENT_RED_VALUE);
    ConfigValue transparentGreenValue = new ConfigValue(EGL14.EGL_TRANSPARENT_GREEN_VALUE);
    ConfigValue transparentBlueValue = new ConfigValue(EGL14.EGL_TRANSPARENT_BLUE_VALUE);
    ConfigBoolValue bindToTextureRgb = new ConfigBoolValue(EGL14.EGL_BIND_TO_TEXTURE_RGB, EGL14.EGL_TRUE);
    ConfigBoolValue bindToTextureRgba = new ConfigBoolValue(EGL14.EGL_BIND_TO_TEXTURE_RGBA, EGL14.EGL_TRUE);
    ConfigValue minSwapInterval = new ConfigValue(EGL14.EGL_MIN_SWAP_INTERVAL);
    ConfigValue maxSwapInterval = new ConfigValue(EGL14.EGL_MAX_SWAP_INTERVAL);
    ConfigValue luminanceSize = new ConfigValue(EGL14.EGL_LUMINANCE_SIZE);
    ConfigValue alphaMaskSize = new ConfigValue(EGL14.EGL_ALPHA_MASK_SIZE);
    ConfigBoolValue colorRgbBufferType = new ConfigBoolValue(EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER);
    ConfigBoolValue colorLuminanceBufferType = new ConfigBoolValue(EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_LUMINANCE_BUFFER);

    ConfigBoolValue renderable30Type = new ConfigBoolValue(EGL14.EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR);
    ConfigBoolValue renderable20Type = new ConfigBoolValue(EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT);
    ConfigBoolValue renderable10Type = new ConfigBoolValue(EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES_BIT);
    ConfigBoolValue conFormant30 = new ConfigBoolValue(EGL14.EGL_CONFORMANT, EGLExt.EGL_OPENGL_ES3_BIT_KHR);
    ConfigBoolValue conFormant20 = new ConfigBoolValue(EGL14.EGL_CONFORMANT, EGL14.EGL_OPENGL_ES2_BIT);
    ConfigBoolValue conFormant10 = new ConfigBoolValue(EGL14.EGL_CONFORMANT, EGL14.EGL_OPENGL_ES_BIT);

    ConfigBoolValue recordableAndroid = new ConfigBoolValue(EGLExt.EGL_RECORDABLE_ANDROID, EGL14.EGL_TRUE);

    private final int[] temp = new int[1];

    private final EGLConfig eglConfig;


    public EnvConfigImpl(EGLDisplay display, EGLConfig config) {
        eglConfig = config;
        for (ConfigValue configValue : configValueList) {
            if (EGL14.eglGetConfigAttrib(display, config, configValue.key, temp, 0)) {
                configValue.setValue(temp[0]);
            } else {
               GLEnvException.clear();
            }
        }
    }


    @Override
    public int getBufferSize() {
        return bufferSize.value;
    }


    @Override
    public int getAlphaSize() {
        return alphaSize.value;
    }


    @Override
    public int getBlueSize() {
        return blueSize.value;
    }


    @Override
    public int getGreenSize() {
        return greenSize.value;
    }


    @Override
    public int getRedSize() {
        return redSize.value;
    }


    @Override
    public int getDepthSize() {
        return depthSize.value;
    }


    @Override
    public int getStencilSize() {
        return stencilSize.value;
    }


    @Override
    public boolean isSlow() {
        return slowCaveat.hasValue;
    }


    @Override
    public int getConfigId() {
        return configId.value;
    }


    @Override
    public int getLevel() {
        return level.value;
    }

    @Override
    public int getMaxPBufferHeight() {
        return maxPbufferHeight.value;
    }


    @Override
    public int getMaxPBufferPixels() {
        return maxPbufferPixels.value;
    }


    @Override
    public int getMaxPBufferWidth() {
        return maxPbufferWidth.value;
    }


    @Override
    public boolean isNativeRenderable() {
        return nativeRenderable.hasValue;
    }


    @Override
    public int getNativeVisualId() {
        return nativeVisualId.value;
    }


    @Override
    public int getNativeVisualType() {
        return nativeVisualType.value;
    }


    @Override
    public int getSamples() {
        return samples.value;
    }


    @Override
    public int getSampleBuffers() {
        return sampleBuffers.value;
    }


    @Override
    public boolean isWindowSurface() {
        return windowSurfaceType.hasValue;
    }


    @Override
    public boolean isPBufferSurface() {
        return pbufferSurfaceType.hasValue;
    }


    @Override
    public boolean isTransparent() {
        return transparentType.hasValue;
    }


    @Override
    public int getTransparentRedValue() {
        return transparentRedValue.value;
    }


    @Override
    public int getTransparentGreenValue() {
        return transparentGreenValue.value;
    }


    @Override
    public int getTransparentBlueValue() {
        return transparentBlueValue.value;
    }


    @Override
    public boolean isBindTextureRgb() {
        return bindToTextureRgb.hasValue;
    }


    @Override
    public boolean isBindToTextureRgba() {
        return bindToTextureRgba.hasValue;
    }


    @Override
    public int getMinSwapInterval() {
        return minSwapInterval.value;
    }


    @Override
    public int getMaxSwapInterval() {
        return maxSwapInterval.value;
    }


    @Override
    public int getLuminanceSize() {
        return luminanceSize.value;
    }


    @Override
    public int getAlphaMaskSize() {
        return alphaMaskSize.value;
    }


    @Override
    public boolean isRgbColor() {
        return colorRgbBufferType.hasValue;
    }


    @Override
    public boolean isLuminanceColor() {
        return colorLuminanceBufferType.hasValue;
    }


    @Override
    public boolean isRenderGL30() {
        return renderable30Type.hasValue;
    }


    @Override
    public boolean isRenderGL20() {
        return renderable20Type.hasValue;
    }


    @Override
    public boolean isRenderGL10() {
        return renderable10Type.hasValue;
    }


    @Override
    public boolean isConformantGL30() {
        return conFormant30.hasValue;
    }


    @Override
    public boolean isConformantGL20() {
        return conFormant20.hasValue;

    }


    @Override
    public boolean isConformantGL10() {
        return conFormant10.hasValue;
    }


    @Override
    public boolean isRecordable() {
        return recordableAndroid.hasValue;
    }


    @Override
    public EGLConfig getEGLConfig() {
        return eglConfig;
    }


    class ConfigValue {
        private int key;
        int value;

        public ConfigValue(int key) {
            this.key = key;
            configValueList.add(this);
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    class ConfigBoolValue extends ConfigValue {
        boolean hasValue;
        int flag;

        public ConfigBoolValue(int key, int flag) {
            super(key);
            this.flag = flag;
        }

        @Override
        public void setValue(int value) {
            super.setValue(value);
            hasValue = (value & flag) != 0;
        }
    }

}
