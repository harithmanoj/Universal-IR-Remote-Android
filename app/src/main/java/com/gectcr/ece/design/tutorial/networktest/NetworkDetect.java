package com.gectcr.ece.design.tutorial.networktest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class NetworkDetect extends AppCompatActivity {

    NetworkManager _networkManager;

    private TextView _service;
    private Handler _updateHandler;

    public static final String TAG = "NetworkDetect";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_detect);
    }
}