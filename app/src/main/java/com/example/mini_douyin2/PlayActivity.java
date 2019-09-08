package com.example.mini_douyin2;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class PlayActivity extends AppCompatActivity {
    private FrameLayout frameLayout;
    private VideoView play_video_view;
    private String string_information;
    private String useid;
    private String usename;
    private String imageurl;
    private String videourl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        frameLayout = findViewById(R.id.play_frame);

        play_video_view = findViewById(R.id.play_video_view);
        Intent intent = getIntent();
        Bundle bundle=intent.getExtras();
        useid=bundle.getString("useid");
        usename=bundle.getString("usename");
        imageurl=bundle.getString("imageurl");
        videourl=bundle.getString("videourl");
        string_information="useid:"+useid+"//usename:"+usename;

        TextView textView1=findViewById(R.id.author_name);
        textView1.append(" "+useid+"  "+usename);
        TextView textView2=findViewById(R.id.up_time);
        textView2.append(" "+System.currentTimeMillis());

        play_video_view.setVideoPath(videourl);
        android.widget.MediaController mc=new android.widget.MediaController(PlayActivity.this);
        play_video_view.setMediaController(mc);
        play_video_view.requestFocus();
        play_video_view.start();

        play_video_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (play_video_view.isPlaying()){
                    play_video_view.pause();
                }
                else {
                    play_video_view.start();
                }
                return false;
            }
        });


        play_video_view.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(PlayActivity.this, "视频播放结束", Toast.LENGTH_SHORT).show();
            }
        });

    }






}
