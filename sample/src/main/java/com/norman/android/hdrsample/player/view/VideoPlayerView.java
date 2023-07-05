package com.norman.android.hdrsample.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.norman.android.hdrsample.player.VideoPlayer;

public class VideoPlayerView extends FrameLayout implements VideoView {
    private AspectRatioSurfaceView surfaceView;

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
        surfaceView = new AspectRatioSurfaceView(getContext());
        FrameLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        addView(surfaceView, layoutParams);
    }


    @Override
    public void setVideoPlayer(VideoPlayer videoPlayer) {
        surfaceView.setVideoPlayer(videoPlayer);
    }

    class AspectRatioSurfaceView extends SurfaceView implements SurfaceHolder.Callback2, VideoPlayer.VideoSizeChangeListener {
        float videoAspectRatio;
        VideoPlayer videoPlayer;

        public AspectRatioSurfaceView(Context context) {
            super(context);
            getHolder().addCallback(this);
        }

        public void setVideoPlayer(VideoPlayer newPlayer) {
            if (videoPlayer == newPlayer) {
                return;
            }
            if (videoPlayer != null) {
                videoPlayer.setSurface(null);
                videoPlayer.removeSizeChangeListener(this);
            }
            videoPlayer = newPlayer;
            if (videoPlayer == null) {
                return;
            }
            Surface surface = getHolder().getSurface();
            if (surface.isValid()) {
                videoPlayer.setSurface(surface);
            }
            onVideoSizeChange(videoPlayer.getWidth(), videoPlayer.getHeight());
            videoPlayer.addSizeChangeListener(this);
        }


        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
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
            videoPlayer.waitFrame();
        }

        @Override
        public void surfaceRedrawNeededAsync(@NonNull SurfaceHolder holder, @NonNull Runnable drawingFinished) {
            SurfaceHolder.Callback2.super.surfaceRedrawNeededAsync(holder, drawingFinished);
        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            videoPlayer.setSurface(holder.getSurface());
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            videoPlayer.setSurface(null);
        }

        @Override
        public void onVideoSizeChange(int width, int height) {
            videoAspectRatio = width * 1.0f / height;
            requestLayout();
        }
    }
}
