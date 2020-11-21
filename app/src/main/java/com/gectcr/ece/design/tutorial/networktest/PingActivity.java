package com.gectcr.ece.design.tutorial.networktest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class PingActivity extends AppCompatActivity {

    private String _outData;
    private String _inData;
    protected static Handler _updateHandler;
    private NetworkConnect _connection;

    public static final String TAG = "PingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);
        _updateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Integer bit = msg.getData().getInt("bit");
                pingMe(bit);
            }
        };
    }

    public void clickPingHigh(View view) {
        pingNow(1);
    }

    public void clickPingLow(View view) {
        pingNow(0);
    }

    private void pingNow(int var) {

        if(_outData.length() == 8)  {
            _outData = _outData.substring(1);
        }
        _outData = _outData + String.valueOf(var);

        ((TextView)findViewById(R.id.outData)).setText("Out: " + _outData);

        ((TextView)findViewById(R.id.outCurrent)).setText(String.valueOf(var));
        Log.i(TAG,"pinging with " + String.valueOf(var));
        _connection.sendMessage(var);
    }

    public void pingMe(int var) {

        if(_inData.length() == 8) {
            _inData = _inData.substring(1);
        }
        _inData = _inData + String.valueOf(var);

        ((TextView)findViewById(R.id.inData)).setText(" In: " + _inData);

        ((TextView)findViewById(R.id.inCurrent)).setText(String.valueOf(var));
        Log.i(TAG,"pinged with " + String.valueOf(var));
    }
}