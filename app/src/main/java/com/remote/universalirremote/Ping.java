package com.remote.universalirremote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.widget.TextView;

public class Ping extends AppCompatActivity {

    HttpClient _httpConnection;
    TextView _sentMessage;
    TextView _recievedMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);
        _sentMessage = (TextView) findViewById(R.id.txt_leftSent);
        _recievedMessage = (TextView) findViewById(R.id.txt_rightRecieved);
    }

    @Override
    protected void onStart() {
        Intent intent = getIntent();
        _httpConnection = new HttpClient((NsdServiceInfo)intent.getParcelableExtra(Constant.INT_SERVICE_KEY));
        _httpConnection.connect();
        super.onStart();
    }

    public void 
}