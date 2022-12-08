package com.ss.video.rtc.demo.advanced;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ss.rtc.demo.advanced.R;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    public void enterRoom(View view) {
        Intent intent = new Intent(StartActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void createRoom(View view) {
        Intent intent = new Intent(StartActivity.this, RoomCreation.class);
        startActivity(intent);
    }
}