package com.gectcr.ece.design.tutorial.networktest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LauncherActivity extends AppCompatActivity {

    public static final String CONNECTION_MODEL = "model.conn";
    public static final Integer SERVER_CONNECTION = 195;
    public static final Integer CLIENT_CONNECTION = 312;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
    }

    public void clickLaunchServer(View view) {
        Intent intent = new Intent(this, PingActivity.class);
        intent.putExtra(CONNECTION_MODEL, SERVER_CONNECTION);
        startActivity(intent);
    }

    public void clickLaunchClient(View view) {
        Intent intent = new Intent(this, DiscoverActivity.class);
        startActivity(intent);
    }
}