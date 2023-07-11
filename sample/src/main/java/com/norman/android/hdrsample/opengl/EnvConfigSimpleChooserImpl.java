package com.norman.android.hdrsample.opengl;

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

        if ((!o1.isWindowSurface() ||
                !o1.isPBufferSurface()) && (o2.isWindowSurface() &&
                o2.isPBufferSurface())) {
            return 1;
        }

        if ((!o1.isRenderGL20() ||
                !o1.isRenderGL30()) && (o2.isRenderGL20() &&
                o2.isConformantGL30())) {
            return 1;
        }
        if (o1.getAlphaMaskSize() > o2.getAlphaMaskSize()) {
            return 1;
        }

        if (o1.isSlow() && !o2.isSlow()) {
            return 1;
        }
        if (!o1.isRecordable() && o2.isRecordable()) {
            return 1;
        }
        if (o1.isTransparent() && !o2.isTransparent()) {
            return 1;
        }
        if (o1.isLuminanceColor() && !o2.isLuminanceColor()) {
            return 1;
        }
        return o1.getConfigId() - o2.getConfigId();
    };
    private final int alphaSize;
    private final int blueSize;
    private final int greenSize;
    private final int redSize;
    private final int depthSize;
    private final int stencilSize;
    private final int samples;
    private final int sampleBuffers;
    private final boolean windowSurface;
    private final boolean pbufferSurface;
    private final Boolean renderGL10;
    private final Boolean renderGL20;
    private final Boolean renderGL30;
    private final Boolean recordable;


    public EnvConfigSimpleChooserImpl(GLEnvConfigSimpleChooser.Builder builder) {
        alphaSize = builder.alphaSize;
        blueSize = builder.blueSize;
        greenSize = builder.greenSize;
        redSize = builder.redSize;
        depthSize = builder.depthSize;
        stencilSize = builder.stencilSize;
        samples =builder.samples;
        sampleBuffers = builder.sampleBuffers;
        windowSurface = builder.windowSurface;
        pbufferSurface = builder.pbufferSurface;
        renderGL10 = builder.renderGL10;
        renderGL20 = builder.renderGL20;
        renderGL30 = builder.renderGL30;
        recordable = builder.recordable;
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
