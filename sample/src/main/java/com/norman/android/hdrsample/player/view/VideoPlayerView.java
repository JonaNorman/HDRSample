package com.norman.android.hdrsample.player.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.norman.android.hdrsample.player.VideoPlayer;

public class VideoPlayerView extends FrameLayout implements VideoView {

    private static final float WAIT_FRAME_DURATION = 0.25f;

    private static final int DEFAULT_VIEW_TYPE = VideoView.VIEW_TYPE_SURFACE_VIEW;
    private AspectRatioPlayerView ratioPlayerView;

    private int viewType;

    public VideoPlayerView(@NonNull Context context) {
        super(context);
        init();
    }

    public VideoPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public VideoPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setViewType(DEFAULT_VIEW_TYPE);
    }


    @Override
    public void setVideoPlayer(VideoPlayer videoPlayer) {
        ratioPlayerView.setVideoPlayer(videoPlayer);
    }

    @Override
    public void setViewType(@ViewType int viewType) {
        if (viewType != this.viewType){
            this.viewType = viewType;
            VideoPlayer videoPlayer = null;
            if (ratioPlayerView instanceof View){
                removeView((View) ratioPlayerView);
                videoPlayer = ratioPlayerView.getVideoPlayer();
                ratioPlayerView.setVideoPlayer(null);
            }
            FrameLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.CENTER;
            if (this.viewType == VIEW_TYPE_SURFACE_VIEW){
                ratioPlayerView = new AspectRatioSurfaceView(getContext());
            }else {
                ratioPlayerView = new AspectRatioTextureView(getContext());
            }
            ratioPlayerView.setVideoPlayer(videoPlayer);
            addView((View) ratioPlayerView, layoutParams);
        }
    }

   static class AspectRatioTextureView extends TextureView implements TextureView.SurfaceTextureListener,AspectRatioPlayerView {

        AspectVideoPlayerHelper videoPlayerHelper;


        public AspectRatioTextureView(Context context) {
            super(context);
            setSurfaceTextureListener(this);
            videoPlayerHelper = new AspectVideoPlayerHelper(this);
        }

        @Override
        public void setVideoPlayer(VideoPlayer player) {
            videoPlayerHelper.setVideoPlayer(player);
        }

        @Override
        public VideoPlayer getVideoPlayer() {
            return videoPlayerHelper.videoPlayer;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            float videoAspectRatio = videoPlayerHelper.getVideoAspectRatio();
            if (Float.isNaN(videoAspectRatio) || videoAspectRatio <= 0) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int originalMeasuredWidth = getMeasuredWidth();
            int originalMeasuredHeight = getMeasuredHeight();
            float originalAspectRatio = originalMeasuredWidth * 1.0f / originalMeasuredHeight;
            int width, height;
            if (originalAspectRatio > videoAspectRatio) {
                width = (int) (originalMeasuredHeight * videoAspectRatio);
                height = originalMeasuredHeight;
            } else {
                width = originalMeasuredWidth;
                height = (int) (originalMeasuredWidth / videoAspectRatio);
            }
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }

        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            videoPlayerHelper.setSurface(surface);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            videoPlayerHelper.setSurface((Surface) null);
            return true;
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }
    }

   static class AspectRatioSurfaceView extends SurfaceView implements SurfaceHolder.Callback2,AspectRatioPlayerView {

        private final AspectVideoPlayerHelper videoPlayerHelper;

        public AspectRatioSurfaceView(Context context) {
            super(context);
            getHolder().addCallback(this);
            videoPlayerHelper = new AspectVideoPlayerHelper(this);
        }

        @Override
        public void setVideoPlayer(VideoPlayer videoPlayer) {
            videoPlayerHelper.setVideoPlayer(videoPlayer);
        }

        @Override
        public VideoPlayer getVideoPlayer() {
            return videoPlayerHelper.videoPlayer;
        }


        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            float videoAspectRatio = videoPlayerHelper.getVideoAspectRatio();
            if (Float.isNaN(videoAspectRatio) || videoAspectRatio <= 0) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int originalMeasuredWidth = getMeasuredWidth();
            int originalMeasuredHeight = getMeasuredHeight();
            float originalAspectRatio = originalMeasuredWidth * 1.0f / originalMeasuredHeight;
            int width, height;
            if (originalAspectRatio > videoAspectRatio) {
                width = (int) (originalMeasuredHeight * videoAspectRatio);
                height = originalMeasuredHeight;
            } else {
                width = originalMeasuredWidth;
                height = (int) (originalMeasuredWidth / videoAspectRatio);
            }
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }

        @Override
        public void surfaceRedrawNeeded(@NonNull SurfaceHolder holder) {
            videoPlayerHelper.waitFrame();
        }

        @Override
        public void surfaceRedrawNeededAsync(@NonNull SurfaceHolder holder, @NonNull Runnable drawingFinished) {
            SurfaceHolder.Callback2.super.surfaceRedrawNeededAsync(holder, drawingFinished);
        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            videoPlayerHelper.setSurface(holder);
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            videoPlayerHelper.setSurface((Surface) null);
        }
    }

    static class AspectVideoPlayerHelper implements VideoPlayer.VideoSizeChangeListener {
        float videoAspectRatio;
        VideoPlayer videoPlayer;
        Surface surface;
        View view;


        public AspectVideoPlayerHelper(SurfaceView surfaceView) {
            view = surfaceView;
            surface = surfaceView.getHolder().getSurface();
        }

        public AspectVideoPlayerHelper(TextureView textureView) {
            view = textureView;
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surface = surfaceTexture == null ? null : new Surface(surfaceTexture);
        }

        public synchronized void setVideoPlayer(VideoPlayer player) {
            if (videoPlayer == player) {
                return;
            }
            if (videoPlayer != null) {
                videoPlayer.setOutputSurface(null);
                videoPlayer.removeSizeChangeListener(this);
            }
            videoPlayer = player;
            if (videoPlayer == null) {
                return;
            }
            if (surface != null && surface.isValid()) {
                videoPlayer.setOutputSurface(surface);
            } else {
                videoPlayer.setOutputSurface(null);
            }
            int width = videoPlayer.getWidth();
            int height = videoPlayer.getHeight();
            if (width != 0 && height != 0){
                onVideoSizeChange(width, height);
            }
            videoPlayer.addSizeChangeListener(this);
        }


        public void waitFrame() {
            videoPlayer.waitNextFrame(WAIT_FRAME_DURATION);
        }

        public synchronized void setSurface(Surface surface) {
            this.surface = surface;
            if (videoPlayer == null) return;
            videoPlayer.setOutputSurface(surface);
        }

        public synchronized void setSurface(SurfaceHolder holder) {
            if (holder == null) {
                videoPlayer.setOutputSurface(null);
            } else {
                videoPlayer.setOutputSurface(holder.getSurface());
            }
        }

        public synchronized void setSurface(SurfaceTexture texture) {
            if (texture == null || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && texture.isReleased())) {
                videoPlayer.setOutputSurface(null);
            } else {
                videoPlayer.setOutputSurface(new Surface(texture));
            }
        }

        public synchronized float getVideoAspectRatio() {
            return videoAspectRatio;
        }

        @Override
        public synchronized void onVideoSizeChange(int width, int height) {
            videoAspectRatio = width * 1.0f / height;
            if (Looper.getMainLooper() == Looper.myLooper()){
                view.requestLayout();
            }else {
                view.post(() -> view.requestLayout());
            }
        }
    }

    interface  AspectRatioPlayerView{
         void setVideoPlayer(VideoPlayer videoPlayer);

         VideoPlayer getVideoPlayer();
    }

}
