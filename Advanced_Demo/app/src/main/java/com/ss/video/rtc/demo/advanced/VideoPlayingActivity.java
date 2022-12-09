package com.ss.video.rtc.demo.advanced;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;
import com.ss.rtc.demo.advanced.R;

import androidx.appcompat.app.AppCompatActivity;

public class VideoPlayingActivity extends AppCompatActivity{
    private VideoView mVideoView;
    private MediaController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_playing_activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(getColor(R.color.white));
            getWindow().setNavigationBarColor(getColor(R.color.white));
        }
        Intent intent = getIntent();
        String mUri = intent.getStringExtra("uri");
        Uri uri = Uri.parse(mUri);
        Log.i("cc_test", mUri);

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this, uri);
        int video_width = Integer.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int video_height = Integer.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

        DisplayMetrics dm = new DisplayMetrics();//屏幕分辨率容器
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screen_height = dm.heightPixels;
        int screen_width = dm.widthPixels;

        if(((double)video_width / (double)video_height) > ((double)screen_width / (double)screen_height)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            this.getWindowManager().getDefaultDisplay().getMetrics(dm);
            screen_height = dm.heightPixels;
        }

        mVideoView = (VideoView) findViewById(R.id.video_view);
        mVideoView.getHolder().setFixedSize((int) (screen_height * video_width / video_height), screen_height);
        mVideoView.forceLayout();
        mVideoView.invalidate();
        controller = new MediaController(this);
        mVideoView.setMediaController(controller);
        mVideoView.setVideoURI(uri);
        mVideoView.start();
    }


    @Override
    public void finish(){
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        super.finish();
    }

}
