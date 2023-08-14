package com.norman.android.hdrsample.player;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class VideoView extends FrameLayout {
    public static final int VIEW_TYPE_SURFACE_VIEW = 1;
    public static final int VIEW_TYPE_TEXTURE_VIEW = 2;

    @IntDef({VIEW_TYPE_SURFACE_VIEW, VIEW_TYPE_TEXTURE_VIEW})
    @Retention(RetentionPolicy.SOURCE)
    @interface ViewType {
    }

    private static final int DEFAULT_VIEW_TYPE = VIEW_TYPE_SURFACE_VIEW;
    private AspectRatioView ratioPlayerView;

    private volatile float aspectRatio;

    private int viewType;

    private Surface surface;

    private int width;
    private int height;

    private SurfaceSubscriber surfaceSubscriber;

    public VideoView(@NonNull Context context) {
        super(context);
        init();
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setViewType(DEFAULT_VIEW_TYPE);
    }


    public void setViewType(@ViewType int viewType) {
        if (viewType != this.viewType) {
            this.viewType = viewType;
            onInternalSurfaceDestroy(surface);
            AspectRatioView oldView = ratioPlayerView;
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    removeView((View) oldView);
                }
            },100);
            FrameLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.CENTER;
            if (this.viewType == VIEW_TYPE_SURFACE_VIEW) {
                ratioPlayerView = new AspectRatioSurfaceView(getContext());
            } else {
                ratioPlayerView = new AspectRatioTextureView(getContext());
            }
            addView((View) ratioPlayerView, layoutParams);
        }
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        if (Looper.getMainLooper() == Looper.myLooper()) {
            requestLayout();
        } else {
            post(this::requestLayout);
        }
    }

    synchronized void onInternalSurfaceAvailable(Surface surface, int width, int height) {
        if (this.surface != null) {
            return;
        }
        this.surface = surface;
        this.width = width;
        this.height = height;
        if (surfaceSubscriber != null)
            surfaceSubscriber.onSurfaceAvailable(surface, width, height);
    }

    synchronized void onInternalSurfaceRedraw(Surface surface) {
        if (this.surface != surface) {
            return;
        }
        if (surfaceSubscriber != null)
            surfaceSubscriber.onSurfaceRedraw();
    }

    synchronized void onInternalSurfaceSizeChange(Surface surface, int width, int height) {
        if (this.surface != surface) {
            return;
        }
        if (width != this.width || this.height != height) {
            this.width = width;
            this.height = height;
            if (surfaceSubscriber != null)
                surfaceSubscriber.onSurfaceSizeChange(width, height);
        }
    }

    synchronized void onInternalSurfaceDestroy(Surface surface) {
        if (this.surface != surface || this.surface == null) {
            return;
        }
        if (surfaceSubscriber != null)
            surfaceSubscriber.onSurfaceDestroy();
        this.surface = null;
    }

    synchronized void subscribe(SurfaceSubscriber subscriber) {
        if (surfaceSubscriber != null && surface != null)
            surfaceSubscriber.onSurfaceDestroy();
        surfaceSubscriber = subscriber;
        if (surface != null && surfaceSubscriber != null)
            surfaceSubscriber.onSurfaceAvailable(surface, width, height);
    }

    synchronized void unsubscribe(SurfaceSubscriber subscriber) {
        if (surfaceSubscriber == subscriber) {
            subscribe(null);
        }
    }

    class AspectRatioTextureView extends TextureView implements TextureView.SurfaceTextureListener, AspectRatioView {
        private Surface surface;

        public AspectRatioTextureView(Context context) {
            super(context);
            setSurfaceTextureListener(this);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            float viewAspectRatio = aspectRatio;
            if (Float.isNaN(viewAspectRatio) || viewAspectRatio <= 0) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int originalMeasuredWidth = getMeasuredWidth();
            int originalMeasuredHeight = getMeasuredHeight();
            float originalAspectRatio = originalMeasuredWidth * 1.0f / originalMeasuredHeight;
            int width, height;
            if (originalAspectRatio > viewAspectRatio) {
                width = (int) (originalMeasuredHeight * viewAspectRatio);
                height = originalMeasuredHeight;
            } else {
                width = originalMeasuredWidth;
                height = (int) (originalMeasuredWidth / viewAspectRatio);
            }
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }

        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
            surface = new Surface(surfaceTexture);
            onInternalSurfaceAvailable(surface, width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            onInternalSurfaceDestroy(surface);
            surface = null;
            return true;
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
            onInternalSurfaceSizeChange(surface, width, height);
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
        }
    }

    class AspectRatioSurfaceView extends SurfaceView implements SurfaceHolder.Callback2, AspectRatioView {

        private Surface surface;

        public AspectRatioSurfaceView(Context context) {
            super(context);
            getHolder().addCallback(this);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            float viewAspectRatio = aspectRatio;
            if (Float.isNaN(viewAspectRatio) || viewAspectRatio <= 0) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int originalMeasuredWidth = getMeasuredWidth();
            int originalMeasuredHeight = getMeasuredHeight();
            float originalAspectRatio = originalMeasuredWidth * 1.0f / originalMeasuredHeight;
            int width, height;
            if (originalAspectRatio > viewAspectRatio) {
                width = (int) (originalMeasuredHeight * viewAspectRatio);
                height = originalMeasuredHeight;
            } else {
                width = originalMeasuredWidth;
                height = (int) (originalMeasuredWidth / viewAspectRatio);
            }
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }

        @Override
        public void surfaceRedrawNeeded(@NonNull SurfaceHolder holder) {
            onInternalSurfaceRedraw(surface);
        }

        @Override
        public void surfaceRedrawNeededAsync(@NonNull SurfaceHolder holder, @NonNull Runnable drawingFinished) {
            SurfaceHolder.Callback2.super.surfaceRedrawNeededAsync(holder, drawingFinished);
        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            surface = holder.getSurface();
            onInternalSurfaceAvailable(surface,getWidth(),getHeight());
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            onInternalSurfaceSizeChange(surface, width, height);
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            onInternalSurfaceDestroy(surface);
            surface = null;
        }
    }


    interface AspectRatioView {

    }


    interface SurfaceSubscriber {
        void onSurfaceAvailable(Surface surface, int width, int height);

        void onSurfaceRedraw();

        void onSurfaceSizeChange(int width, int height);

        void onSurfaceDestroy();
    }


}
