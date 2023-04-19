/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jonanorman.android.hdrsample.player.surface;


import android.graphics.SurfaceTexture;
import android.view.Surface;

import com.jonanorman.android.hdrsample.player.opengl.env.GLEnvHandler;
import com.jonanorman.android.hdrsample.util.GLESUtil;

import java.util.concurrent.Callable;

public final class TextureSurface extends Surface {

    private static GLEnvHandler HANDLER;
    private static int HANDLE_HOLDER_COUNT;

    private static Object HANDLER_LOCK = new Object();

    private SurfaceTexture surfaceTexture;
    private int textureId;

    private Object lock = new Object();

    private TextureSurface(SurfaceTexture texture, int textureId) {
        super(texture);
        this.surfaceTexture = texture;
        this.textureId = textureId;
        this.surfaceTexture.setOnFrameAvailableListener(surfaceTexture -> surfaceTexture.updateTexImage());
        synchronized (HANDLER_LOCK) {
            HANDLE_HOLDER_COUNT++;
        }
    }

    public void setSize(int width, int height) {
        synchronized (lock) {
            if (!isValid()) {
                return;
            }
            this.surfaceTexture.setDefaultBufferSize(width, height);
        }
    }

    public void getTransformMatrix(float[] matrix) {
        synchronized (lock) {
            if (!isValid()) {
                return;
            }
            this.surfaceTexture.getTransformMatrix(matrix);
        }
    }

    public long getTimestamp() {
        synchronized (lock) {
            if (!isValid()) {
                return 0;
            }
            return this.surfaceTexture.getTimestamp();
        }
    }


    @Override
    public void release() {
        synchronized (lock) {
            if (!isValid()) {
                return;
            }
            super.release();
            this.surfaceTexture.release();
            synchronized (HANDLER_LOCK) {
                HANDLE_HOLDER_COUNT--;
                if (HANDLE_HOLDER_COUNT == 0) {
                    if (HANDLER != null) {
                        HANDLER.recycle();
                        HANDLER = null;
                    }
                } else {
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            GLESUtil.delTextureId(textureId);
                        }
                    });
                }
            }
        }
    }

    @Override
    public synchronized boolean isValid() {
        return super.isValid();
    }

    public static TextureSurface create() {
        GLEnvHandler messageHandler = getGLEnvHandler();
        return messageHandler.submitAndWait(new Callable<TextureSurface>() {
            @Override
            public TextureSurface call() {
                int textureId = GLESUtil.createExternalTextureId();
                SurfaceTexture surfaceTexture = new SurfaceTexture(textureId) {
                    boolean release = false;
                    boolean finalize = false;

                    @Override
                    public synchronized void release() {
                        if (release || finalize) return;
                        super.release();
                        release = false;
                    }

                    @Override
                    protected synchronized void finalize() throws Throwable {
                        finalize = true;
                        super.finalize();
                    }
                };
                return new TextureSurface(surfaceTexture, textureId);
            }
        });

    }

    private static GLEnvHandler getGLEnvHandler() {
        synchronized (HANDLER_LOCK) {
            if (HANDLER != null && !HANDLER.isRecycle()) {
                return HANDLER;
            }
            HANDLER = GLEnvHandler.create();
            return HANDLER;
        }
    }


}
