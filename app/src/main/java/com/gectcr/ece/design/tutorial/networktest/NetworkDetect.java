package com.gectcr.ece.design.tutorial.networktest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

public class NetworkDetect extends AppCompatActivity {

    NetworkManager _networkManager;

    private TextView _service;
    private Spinner _discoveredServices;
    private Handler _updateHandler;

    NetworkConnect _connection;

    public static final String TAG = "NetworkDetect";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_detect);
        _service = (TextView) findViewById(R.id.service);
        _discoveredServices = (Spinner) findViewById(R.id.disc_list);
    }

    public void clickRegister(View view) {
        if(_connection.getLocalPort() > -1) {
            _networkManager.registerService(_connection.getLocalPort());
            _service.setText(_networkManager._serviceName + " " + _networkManager.SERVICE_TYPE);
        } else {
            Log.d(TAG, "Server Socket unbound");
        }
    }


}