package com.example.bogdi.bogdan;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {

    private String current;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set fullscreen activity without title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        //play video intro
        String path;
        VideoView mVideoView;
        path = "android.resource://com.example.bogdi.bogdan/" + R.raw.video_intro;
        mVideoView = (VideoView) findViewById(R.id.video);
        if (path.length() == 0) {
            Toast.makeText(MainActivity.this, "File URL/path is empty",
                    Toast.LENGTH_LONG).show();

        } else {
            // If the path has not changed, just start the media player
            if (path.equals(current) && mVideoView != null) {
                mVideoView.start();
                mVideoView.requestFocus();
                return;
            }
            current = path;
            if (mVideoView != null) {
                mVideoView.setVideoPath(path);
            }
            if (mVideoView != null) {
                mVideoView.start();
            }
            mVideoView.requestFocus();
        }

        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent in = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(in);
                finish();
                return false;
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                Intent in = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(in);
                finish();
            }
        });


    }
}
