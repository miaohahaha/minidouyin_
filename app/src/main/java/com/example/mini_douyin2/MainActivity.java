package com.example.mini_douyin2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    private String[] PermissionsArray = new String[]{CAMERA,WRITE_EXTERNAL_STORAGE,RECORD_AUDIO,INTERNET};
    private int[] grantResults = new int[]{};
    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final int REQUEST_PERMISSION =10;

    private RecyclerView mRecyclerView;
    private List<Feed> mFeeds = new ArrayList<>();

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Button bt_request = findViewById(R.id.bt_request_video);
        //Button bt_camera = findViewById(R.id.bt_camera);
        //Button bt_upload = findViewById(R.id.bt_upload);
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("permission","click check_permission");
            if (!checkPermissionAllGranted(PermissionsArray)) {
                Log.d("permission","click not_all_permission");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.d("permission","click SDK bigger");
                    requestPermissions(PermissionsArray, REQUEST_PERMISSION);

                }
            }
            onRequestPermissionsResult(REQUEST_PERMISSION, PermissionsArray, grantResults);
        }



        findViewById(R.id.bt_camera).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RecordVideoActivity.class));
        });
        findViewById(R.id.bt_upload).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, video.class));
        });

        initRecycleListView();

        findViewById(R.id.bt_request_video).setOnClickListener(v -> {
            Log.d("request","click to request video");
            fetchFeed();
        });

    }

    private void initRecycleListView(){
        mRecyclerView = findViewById(R.id.rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                ImageView imageView = new ImageView(viewGroup.getContext());
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                imageView.setAdjustViewBounds(true);
                return new MainActivity.MyViewHolder(imageView);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
                ImageView iv = (ImageView) viewHolder.itemView;
                iv.setMinimumHeight(mRecyclerView.getHeight());
                Glide.with(iv.getContext()).load(mFeeds.get(position).getImageUrl()).into(iv);


                Bundle bundle = new Bundle();
                bundle.putString("useid",mFeeds.get(position).getStudentId());
                bundle.putString("usename",mFeeds.get(position).getUserName());
                bundle.putString("imageurl",mFeeds.get(position).getImageUrl());
                bundle.putString("videourl",mFeeds.get(position).getVideoUrl());
                final Intent intent= new Intent(MainActivity.this, PlayActivity.class);
                intent.putExtras(bundle);

                iv.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(MainActivity.this, intent.getExtras().getString("videourl"), Toast.LENGTH_LONG).show();
                        startActivity(intent);
                    }
                });

            }

            @Override
            public int getItemCount() {
                return mFeeds.size();
            }
        });


    }

    public void fetchFeed() {
        Button button = findViewById(R.id.bt_request_video);
        button.setText("requesting");
        findViewById(R.id.bt_request_video).setEnabled(false);
        RetrofitManager.get(MinidouyinService.HOST).create(MinidouyinService.class).fetchFeed().enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                Log.d("TAG", "click onResponse() called with: call = [" + call + "], response = [" + response.body() + "]");
                if (response.isSuccessful()) {
                    mFeeds = response.body().getFeeds();
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                    Log.d("tag","click size"+ mFeeds.size());
                    Toast.makeText(MainActivity.this, "size:"+mFeeds.size(), Toast.LENGTH_LONG).show();
                } else {
                    //Log.d(TAG, "onResponse() called with: response.errorBody() = [" + response.errorBody() + "]");
                    Toast.makeText(MainActivity.this, "fetch feed failure!", Toast.LENGTH_LONG).show();
                }
                resetBtn();
            }

            @Override public void onFailure(Call<FeedResponse> call, Throwable t) {
                //Log.d(TAG, "onFailure() called with: call = [" + call + "], t = [" + t + "]");
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                resetBtn();
            }

            private void resetBtn() {
                button.setText("refresh feed");
                findViewById(R.id.bt_request_video).setEnabled(true);
            }
        });
    }


    private boolean checkPermissionAllGranted(String[] permissions) {
        // 6.0以下
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false

                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            Toast.makeText(this, "已授权" + Arrays.toString(permissions), Toast.LENGTH_LONG).show();
        }
    }



}
