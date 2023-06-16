package com.norman.android.hdrsample;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.norman.android.hdrsample.player.AndroidSurfacePlayer;
import com.norman.android.hdrsample.player.AndroidTexturePlayer;
import com.norman.android.hdrsample.player.AndroidVideoPlayer;
import com.norman.android.hdrsample.player.Player;
import com.norman.android.hdrsample.player.source.FileSource;
import com.norman.android.hdrsample.player.view.VideoSurfaceView;

public class HDRPlayActivity extends AppCompatActivity {
    AndroidVideoPlayer videoPlayer;
    VideoSurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_hdr_player);
        surfaceView = findViewById(R.id.SurfaceView);
        videoPlayer = AndroidSurfacePlayer.create();
        videoPlayer.setSource(FileSource.createForAsset("video/1.mp4"));
        surfaceView.setVideoPlayer(videoPlayer);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPlayer.release();
    }
}