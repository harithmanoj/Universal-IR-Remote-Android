package com.gectcr.ece.design.tutorial.networktest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
    }

    public void clickLaunchServer(View view) {

    }

    public void clickLaunchClient(View view) {
        Intent intent = new Intent(this, DiscoverActivity.class);
        startActivity(intent);
    }
}