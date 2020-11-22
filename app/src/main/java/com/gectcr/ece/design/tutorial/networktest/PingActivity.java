package com.gectcr.ece.design.tutorial.networktest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class PingActivity extends AppCompatActivity {

    private String _outData;
    private static final String OUT_DATA_KEY = "out.stream.ui";
    private String _inData;
    private static final String IN_DATA_KEY = "in.stream.ui";
    private TextView _connectionStatus;
    private TextView _outStreamUI;
    private TextView _inStreamUI;
    private TextView _outCurrentUI;
    private static final String OUT_CURRENT_KEY = "out.curr.ui";
    private TextView _inCurrentUI;
    private static final String IN_CURRENT_KEY = "in.curr.ui";

    public static final String TAG = "PingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);
        String in = "0", out = "0";

        _connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        _outStreamUI = (TextView) findViewById(R.id.outData);
        _inStreamUI = (TextView) findViewById(R.id.inData);
        _outCurrentUI = (TextView) findViewById(R.id.outCurrent);
        _inCurrentUI = (TextView) findViewById(R.id.inCurrent);

        if (savedInstanceState.isEmpty()) {
           _outData = "Out : ";
           _inData = "In : ";
        } else {
            _outData = savedInstanceState.getString(OUT_DATA_KEY);
            _inData = savedInstanceState.getString(IN_DATA_KEY);
            in = savedInstanceState.getString(IN_CURRENT_KEY);
            out = savedInstanceState.getString(OUT_CURRENT_KEY);
        }
        _inCurrentUI.setText(in);
        _outCurrentUI.setText(out);


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
        // connection send message

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
}