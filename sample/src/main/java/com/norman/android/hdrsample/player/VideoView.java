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

    private static final int DELAY_REMOVE_VIEW_TIME_MS = 250;

    private static final int DEFAULT_VIEW_TYPE = VIEW_TYPE_SURFACE_VIEW;
    private View currentView;

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
        if (viewType == this.viewType) {
          return;
        }
        this.viewType = viewType;
        onSurfaceDestroy(surface);
        View oldView = currentView;
        postDelayed(() -> removeView(oldView),DELAY_REMOVE_VIEW_TIME_MS);
        FrameLayout.LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        currentView = this.viewType == VIEW_TYPE_SURFACE_VIEW ?
                new AspectRatioSurfaceView(getContext()) :
                new AspectRatioTextureView(getContext());
        addView(currentView, layoutParams);
    }


    public void setAspectRatio(float aspectRatio) {
        if (aspectRatio <= 0)return;
        this.aspectRatio = aspectRatio;
        View view = currentView;
        if (Looper.getMainLooper() == Looper.myLooper()) {
            view.requestLayout();
        } else {
            post(view::requestLayout);
        }
    }

    synchronized void onSurfaceAvailable(Surface surface, int width, int height) {
        if (this.surface != null) {
            return;
        }
        this.surface = surface;
        this.width = width;
        this.height = height;
        if (surfaceSubscriber != null)
            surfaceSubscriber.onSurfaceAvailable(surface, width, height);
    }

    synchronized void onSurfaceRedraw(Surface surface) {
        if (this.surface != surface) {
            return;
        }
        if (surfaceSubscriber != null)
            surfaceSubscriber.onSurfaceRedraw();
    }

    synchronized void onSurfaceSizeChange(Surface surface, int width, int height) {
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

    synchronized void onSurfaceDestroy(Surface surface) {
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

    class AspectRatioTextureView extends TextureView implements TextureView.SurfaceTextureListener{
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
            onSurfaceAvailable(surface, width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            onSurfaceDestroy(surface);
            surface = null;
            return true;
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
            onSurfaceSizeChange(surface, width, height);
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
        }
    }

    class AspectRatioSurfaceView extends SurfaceView implements SurfaceHolder.Callback2 {

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
            onSurfaceRedraw(surface);
        }

        @Override
        public void surfaceRedrawNeededAsync(@NonNull SurfaceHolder holder, @NonNull Runnable drawingFinished) {
            SurfaceHolder.Callback2.super.surfaceRedrawNeededAsync(holder, drawingFinished);
        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            surface = holder.getSurface();
            onSurfaceAvailable(surface,getWidth(),getHeight());
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            onSurfaceSizeChange(surface, width, height);
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            onSurfaceDestroy(surface);
            surface = null;
        }
    }


    interface SurfaceSubscriber {
        void onSurfaceAvailable(Surface surface, int width, int height);

        void onSurfaceRedraw();

        void onSurfaceSizeChange(int width, int height);

        void onSurfaceDestroy();
    }


}
