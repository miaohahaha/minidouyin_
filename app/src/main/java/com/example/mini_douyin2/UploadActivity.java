package com.example.mini_douyin2;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class UploadActivity extends AppCompatActivity {

    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    private ImageButton button_resume;
    private ImageButton button_pause;
    private ImageButton button_camera;
    private ImageButton button_DCIM;
    private ImageButton button_up;
    private SeekBar seekBar;
    private Camera camera;
    private String video_name="w2e3.mp4";
    private String video_path;
    private Handler handler;
    private Runnable runnable;
    private Uri uri;

    private SurfaceHolder holder;
    private boolean stop_runnable=false;
    private boolean pause_flag=false;

    private static final String TAG = "ViedoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);


        //button_pause = findViewById(R.id.pause_button);
       // button_DCIM = findViewById(R.id.dcim_button);
        button_resume = findViewById(R.id.resume_button);
        button_up=findViewById(R.id.chuan);
        //seekBar = findViewById(R.id.seekBar);


        button_resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
            }
        });

        button_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.pause();
            }
        });

        button_DCIM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,3);
            }
        });



        handler = new Handler();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(runnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progess = seekBar.getProgress();
                mediaPlayer.seekTo(progess);
                getProgess();
            }
        });

        surfaceView = findViewById(R.id.surfaceview);
        mediaPlayer = new MediaPlayer();
        holder = surfaceView.getHolder();
        holder.addCallback(new PlayerCallBack());
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
                mediaPlayer.setLooping(true);
                seekBar.setMax(mediaPlayer.getDuration());
                getProgess();
            }
        });
        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                Log.i(TAG, "onBufferingUpdate: "+percent);
            }
        });

    }

    private void initmedioplayer(Uri uri){

        try{
            mediaPlayer.setDataSource(this,uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            getProgess();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getProgess(){

        runnable = new Runnable() {
            @Override
            public void run() {
                int progess = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(progess);
                handler.postDelayed(runnable,400);
            }
        };
        handler.post(runnable);

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==2){
            File tempFile = new File(Environment.getExternalStorageDirectory(),video_name);
            video_path = tempFile.getPath();
        }else if(requestCode==3){
            Uri chooseview_uri = data.getData();
            String[] filePathColumn ={MediaStore.Video.Media.DATA};
            Cursor cursor=getContentResolver().query(chooseview_uri,filePathColumn,null,null,null);
            cursor.moveToFirst();
            String path=cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
            try{

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.start();
                seekBar.setProgress(0);
                seekBar.setMax(mediaPlayer.getDuration());
                getProgess();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private class PlayerCallBack implements SurfaceHolder.Callback{
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mediaPlayer.setDisplay(holder);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mediaPlayer.start();
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();
        pause_flag=true;
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        pause_flag=false;
    }





}

