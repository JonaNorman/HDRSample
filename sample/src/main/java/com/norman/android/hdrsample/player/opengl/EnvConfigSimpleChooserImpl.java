package com.norman.android.hdrsample.player.opengl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class EnvConfigSimpleChooserImpl implements GLEnvConfigSimpleChooser {

    private final static Comparator<GLEnvConfig> CONFIG_COMPARATOR = (o1, o2) -> {
        if (o1.getRedSize() > o2.getRedSize()) {
            return 1;
        } else if (o1.getRedSize() < o2.getRedSize()) {
            return -1;
        }
        if (o1.getGreenSize() > o2.getGreenSize()) {
            return 1;
        } else if (o1.getGreenSize() < o2.getGreenSize()) {
            return -1;
        }
        if (o1.getBlueSize() > o2.getBlueSize()) {
            return 1;
        } else if (o1.getBlueSize() < o2.getBlueSize()) {
            return -1;
        }
        if (o1.getAlphaSize() > o2.getAlphaSize()) {
            return 1;
        } else if (o1.getAlphaSize() < o2.getAlphaSize()) {
            return -1;
        }
        if (o1.getDepthSize() > o2.getDepthSize()) {
            return 1;
        } else if (o1.getDepthSize() < o2.getDepthSize()) {
            return -1;
        }
        if (o1.getStencilSize() > o2.getStencilSize()) {
            return 1;
        } else if (o1.getStencilSize() < o2.getStencilSize()) {
            return -1;
        }
        if (o1.getSampleBuffers() > o2.getSampleBuffers()) {
            return 1;
        } else if (o1.getSampleBuffers() < o2.getSampleBuffers()) {
            return -1;
        }
        if (o1.getSamples() > o2.getSamples()) {
            return 1;
        } else if (o1.getSamples() < o2.getSamples()) {
            return -1;
        }

        if ((o1.isWindowSurface() == false ||
                o1.isPBufferSurface() == false) && (o2.isWindowSurface() == true &&
                o2.isPBufferSurface() == true)) {
            return 1;
        }

        if ((o1.isRenderGL20() == false ||
                o1.isRenderGL30() == false) && (o2.isRenderGL20() == true &&
                o2.isConformantGL30() == true)) {
            return 1;
        }
        if (o1.getAlphaMaskSize() > o2.getAlphaMaskSize()) {
            return 1;
        }

        if (o1.isSlow() == true && o2.isSlow() == false) {
            return 1;
        }
        if (o1.isRecordable() == false && o2.isRecordable() == true) {
            return 1;
        }
        if (o1.isTransparent() == true && o2.isTransparent() == false) {
            return 1;
        }
        if (o1.isLuminanceColor() == true && o2.isLuminanceColor() == false) {
            return 1;
        }
        return o1.getConfigId() - o2.getConfigId();
    };
    private int alphaSize = 8;
    private int blueSize = 8;
    private int greenSize = 8;
    private int redSize = 8;
    private int depthSize;
    private int stencilSize;
    private int samples;
    private int sampleBuffers;
    private boolean windowSurface;
    private boolean pbufferSurface;
    private Boolean renderGL10;
    private Boolean renderGL20;
    private Boolean renderGL30 = true;
    private Boolean recordable;


    public EnvConfigSimpleChooserImpl() {
        this.windowSurface = true;
        this.pbufferSurface = true;
    }

    @Override
    public void setAlphaSize(int alphaSize) {
        this.alphaSize = alphaSize;
    }

    @Override
    public void setBlueSize(int blueSize) {
        this.blueSize = blueSize;
    }

    @Override
    public void setGreenSize(int greenSize) {
        this.greenSize = greenSize;
    }

    @Override
    public void setRedSize(int redSize) {
        this.redSize = redSize;
    }

    @Override
    public void setDepthSize(int depthSize) {
        this.depthSize = depthSize;
    }

    @Override
    public void setStencilSize(int stencilSize) {
        this.stencilSize = stencilSize;
    }

    @Override
    public void setSamples(int samples) {
        this.samples = samples;
    }

    @Override
    public void setSampleBuffers(int sampleBuffers) {
        this.sampleBuffers = sampleBuffers;
    }

    @Override
    public void setRenderGL10(Boolean renderGL10) {
        this.renderGL10 = renderGL10;
    }

    @Override
    public void setRenderGL20(Boolean renderGL20) {
        this.renderGL20 = renderGL20;
    }

    @Override
    public void setRenderGL30(Boolean renderGL30) {
        this.renderGL30 = renderGL30;
    }

    @Override
    public void setRecordable(Boolean recordable) {
        this.recordable = recordable;
    }

    @Override
    public final GLEnvConfig chooseConfig(GLEnvConfig[] configs) {
        List<GLEnvConfig> findConfigs = new ArrayList<>();
        for (GLEnvConfig config : configs) {
            if (config.getRedSize() >= redSize
                    && config.getGreenSize() >= greenSize
                    && config.getBlueSize() >= blueSize
                    && config.getAlphaSize() >= alphaSize
                    && config.getDepthSize() >= depthSize
                    && config.getStencilSize() >= stencilSize
                    && config.getSampleBuffers() >= sampleBuffers
                    && config.getSamples() >= samples
                    && (config.isWindowSurface() == windowSurface)
                    && (config.isPBufferSurface() == pbufferSurface)
                    && (renderGL10 == null || config.isRenderGL10() == renderGL10)
                    && (renderGL20 == null || config.isRenderGL20() == renderGL20)
                    && (renderGL30 == null || config.isRenderGL30() == renderGL30)
                    && (recordable == null || config.isRecordable() == recordable)) {
                findConfigs.add(config);
            }
        }
        Collections.sort(findConfigs, CONFIG_COMPARATOR);
        return findConfigs.size() > 0 ? findConfigs.get(0) : null;
    }

}
