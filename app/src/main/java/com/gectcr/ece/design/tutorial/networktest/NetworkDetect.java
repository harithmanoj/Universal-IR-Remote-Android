package com.gectcr.ece.design.tutorial.networktest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class NetworkDetect extends AppCompatActivity {

    NetworkManager _networkManager;

    private TextView _service;
    private Spinner _discoveredServices;
    private Handler _updateHandler;
    private Handler _discoverHandler;

    NetworkConnect _connection;

    Context _context = this;

    public static final String TAG = "NetworkDetect";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_detect);
        _service = (TextView) findViewById(R.id.service);
        _discoveredServices = (Spinner) findViewById(R.id.disc_list);
        _discoverHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                CharSequence[] list = msg.getData().getCharSequenceArray("services");
                ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(_context, R.layout.support_simple_spinner_dropdown_item, list);
                adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                _discoveredServices.setAdapter(adapter);
            }
        };

    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Starting ");
        _connection = new NetworkConnect(_updateHandler);
        _networkManager = new NetworkManager(this, _discoverHandler);
        _networkManager.initializeResolveListener();

        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Pause");
        if(_networkManager != null) {
            _networkManager.stopDiscovery();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Resume");
        if(_networkManager != null) {
            _networkManager.discoverServices();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "stopped");
        _networkManager.tearDown();;
        _connection.tearDown();
        _networkManager = null;
        _connection = null;
        _discoverHandler = null;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Being destroyed.");
        _networkManager = null;
        _connection = null;
        _discoverHandler = null;
        super.onDestroy();
    }

    public void clickRegister(View view) {
        if(_connection.getLocalPort() > -1) {
            _networkManager.registerService(_connection.getLocalPort());
            _service.setText(_networkManager._serviceName + " " + _networkManager.SERVICE_TYPE);
        } else {
            Log.d(TAG, "Server Socket unbound");
        }
    }

    public void clickDiscover(View view) {
        _networkManager.discoverServices();
    }



}