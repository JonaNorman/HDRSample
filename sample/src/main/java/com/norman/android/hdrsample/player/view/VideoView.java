package com.norman.android.hdrsample.player.view;

import androidx.annotation.IntDef;

import com.norman.android.hdrsample.player.VideoPlayer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface VideoView {

    int VIEW_TYPE_SURFACE_VIEW = 1;
    int VIEW_TYPE_TEXTURE_VIEW = 2;

    @IntDef({VIEW_TYPE_SURFACE_VIEW, VIEW_TYPE_TEXTURE_VIEW})
    @Retention(RetentionPolicy.SOURCE)
    @interface ViewType {
    }

    void setVideoPlayer(VideoPlayer videoPlayer);

    void setViewType(@ViewType int viewType);
}
