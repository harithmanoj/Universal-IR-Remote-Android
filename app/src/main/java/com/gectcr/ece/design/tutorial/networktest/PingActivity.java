package com.gectcr.ece.design.tutorial.networktest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class PingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);
    }

    public void clickPingHigh(View view) {
        pingNow(1);
    }

    public void clickPingLow(View view) {
        pingNow(0);
    }

    private void pingNow(int var) {

        if(outData.length() == 8)  {
            outData = outData.substring(1);
        }
        outData = outData + String.valueOf(var);

        ((TextView)findViewById(R.id.outData)).setText("Out: " + outData);

        ((TextView)findViewById(R.id.outCurrent)).setText(String.valueOf(var));
        Log.i(TAG,"pinging with " + String.valueOf(var));
        connection.sendMessage(String.valueOf(var));
    }

    public void pingMe(int var) {

        if(inData.length() == 8) {
            inData = inData.substring(1);
        }
        inData = inData + String.valueOf(var);

        ((TextView)findViewById(R.id.inData)).setText(" In: " + inData);

        ((TextView)findViewById(R.id.inCurrent)).setText(String.valueOf(var));
        Log.i(TAG,"pinged with " + String.valueOf(var));
    }
}