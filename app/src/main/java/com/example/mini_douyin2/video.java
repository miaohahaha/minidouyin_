package com.example.mini_douyin2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class video extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final String TAG = "Post";
    public Uri mSelectedImage;
    public Uri mSelectedVideo;
    public Button mBtn;
    public ImageView imageView;
    private ImageButton mBtnPost;
    private ImageButton mBtnStart;
    public String studentId;
    public String studentName;
    private MediaPlayer mediaPlayer=null;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private static final String SD_PATH = "/sdcard/myapp/pic/";
    private static final String IN_PATH = "/myapp/pic/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_upload);
        surfaceView= findViewById(R.id.surfaceview);
        surfaceHolder=surfaceView.getHolder();
        initBtns();


        findViewById(R.id.goback).setOnClickListener(view -> {
            startActivity(new Intent(video.this, UploadActivity.class));
        });

        super.onCreate(savedInstanceState);
    }


    private void Showimage()
    {
        imageView=findViewById(R.id.action_image);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageURI(mSelectedImage);
    }
    private void Hintimage()
    {
        imageView.setVisibility(View.INVISIBLE);
    }

    private void bindViews(){
        surfaceHolder.addCallback(this);
        mBtnStart = findViewById(R.id.resume_button);
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Hintimage();
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
                else {
                    mediaPlayer.start();
                }
            }
        });
    }

    private void initBtns() {
        mBtn = findViewById(R.id.dcim_button);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = mBtn.getText().toString();
                if (getString(R.string.select_a_video).equals(s) || getString(R.string.reselect).equals(s)) {
                    chooseVideo();
                }
                else if(getString(R.string.select_a_image).equals(s)){
                    chooseImage();
                }
            }
        });

        mBtnPost = findViewById(R.id.chuan);
        mBtnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toast;
                toast = "ing......";
                Toast.makeText(video.this, toast, Toast.LENGTH_LONG).show();
                postVideo(v);
            }
        });



    }




    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE);

    }


    public void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"),
                PICK_VIDEO);
    }

    public Bitmap getVideoThumb(Uri uri){
        MediaMetadataRetriever media=new MediaMetadataRetriever();
        media.setDataSource(video.this,uri);
        return media.getFrameAtTime();
    }

    public static void saveBitmap(Context context, Bitmap mBitmap) {
        String savePath;
        File filePic;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            savePath = SD_PATH;
        } else {
            savePath = context.getApplicationContext().getFilesDir()
                    .getAbsolutePath()
                    + IN_PATH;
        }
        try {
            filePic = new File(savePath + UUID.randomUUID().toString() + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        return;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        if (resultCode == RESULT_OK && null != data) {
            if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
                Bitmap bitmap=getVideoThumb(mSelectedVideo);
                saveBitmap(this,bitmap);
                Log.d(TAG, "mSelectedVideo = " + mSelectedVideo);
                mBtn.setText(R.string.select_a_image);
            } else if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                Log.d(TAG, "selectedImage = " + mSelectedImage);
                mBtn.setText(R.string.reselect);
                Showimage();  //显示视频
                bindViews();  //开始播放
            }

        }
    }

    private MultipartBody.Part getMultipartFromUri (String name, Uri uri){
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(video.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    private void postVideo (View view) {
       // mBtnPost.setText(R.string.post);
        mBtnPost.setEnabled(false);
        //studentId="888888";
       // studentName="6666";
        EditText editText = findViewById(R.id.s_id);
        studentId = editText.getText().toString();
        editText = findViewById(R.id.s_ming);
        studentName = editText.getText().toString();
        RetrofitManager.get(MinidouyinService.HOST).create(MinidouyinService.class).createVideo(studentId, studentName, getMultipartFromUri("cover_image", mSelectedImage), getMultipartFromUri("video", mSelectedVideo)).enqueue(new Callback<PostVideoResponse>() {
            @Override
            public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                Log.d(TAG, "onResponse() called with: call = [" + call + "], response = [" + response.body() + "]");
                String toast;
                if (response.isSuccessful()) {
                    toast = "Post Success!";
                } else {
                    Log.d(TAG, "onResponse() called with: response.errorBody() = [" + response.errorBody() + "]");
                    toast = "Post Failure...";
                }
                Toast.makeText(video.this, toast, Toast.LENGTH_LONG).show();
                mBtnPost.setEnabled(true);
            }

            @Override
            public void onFailure(Call<PostVideoResponse> call, Throwable t) {
                Log.d(TAG, "onFailure() called with: call = [" + call + "], t = [" + t + "]");
                Toast.makeText(video.this, t.getMessage(), Toast.LENGTH_LONG).show();
                mBtnPost.setEnabled(true);
            }
        });


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer=MediaPlayer.create(video.this,mSelectedVideo);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setDisplay(surfaceHolder);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.onDestroy();
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.release();
    }





}
