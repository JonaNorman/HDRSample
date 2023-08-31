package com.norman.android.hdrsample.player;

import com.norman.android.hdrsample.player.shader.TextureFragmentShader;

class GLTexture2DRenderer extends GLTextureRenderer {

    public GLTexture2DRenderer() {
        super(TextureFragmentShader.TYPE_TEXTURE_2D);
    }
}
