package com.jonanorman.android.hdrsample;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.jonanorman.android.hdrsample.player.Player;
import com.jonanorman.android.hdrsample.player.source.FileSource;
import com.jonanorman.android.hdrsample.player.view.VideoSurfaceView;

public class HDRPlayActivity extends AppCompatActivity {
    OESHDRPlayer videoPlayer;
    VideoSurfaceView surfaceView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// todo
        setContentView(R.layout.activity_hdr_player);
        surfaceView1 = findViewById(R.id.SurfaceView1);
        videoPlayer = new OESHDRPlayer();
        videoPlayer.setSource(FileSource.createAssetFileSource(getApplicationContext(), "1.mp4"));
        videoPlayer.prepare();
        videoPlayer.setCallback(new Player.Callback() {

            @Override
            public void onPlayEnd() {
                videoPlayer.seek(0);
            }


            @Override
            public void onPlayError(Throwable throwable) {
                Log.e("VideoPlayer", "errorMsg:" + throwable.getMessage() + "\n" + "errorStack:" + Log.getStackTraceString(throwable));
            }
        });
        surfaceView1.setVideoPlayer(videoPlayer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPlayer.release();
    }
}