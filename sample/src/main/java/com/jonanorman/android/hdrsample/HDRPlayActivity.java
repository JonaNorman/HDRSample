package com.jonanorman.android.hdrsample;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.jonanorman.android.hdrsample.player.Player;
import com.jonanorman.android.hdrsample.player.VideoSurfacePlayer;
import com.jonanorman.android.hdrsample.player.source.FileSource;

public class HDRPlayActivity extends AppCompatActivity {
    OESHDRPlayer videoPlayer;
    SurfaceView surfaceView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// todo
        setContentView(R.layout.activity_hdr_player);
        surfaceView1 = findViewById(R.id.SurfaceView1);
        videoPlayer = new OESHDRPlayer();
        videoPlayer.setSource(FileSource.createAssetFileSource(getApplicationContext(), "1.mp4"));
        videoPlayer.setCallback(new Player.Callback() {
            @Override
            public void onPlayStart() {

            }

            @Override
            public void onPlayEnd() {
                videoPlayer.seek(0);
            }


            @Override
            public void onPlayError(Throwable throwable) {
                Log.e("VideoPlayer", "errorMsg:" + throwable.getMessage() + "\n" + "errorStack:" + Log.getStackTraceString(throwable));
            }
        });

        videoPlayer.prepare();
        videoPlayer.start();
        surfaceView1.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                videoPlayer.setSurface(holder.getSurface());
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                videoPlayer.setSurface(null);
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoPlayer.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPlayer.release();
    }
}