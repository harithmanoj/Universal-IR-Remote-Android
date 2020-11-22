package com.gectcr.ece.design.tutorial.networktest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class NetworkDetect extends AppCompatActivity {

    protected static NetworkManager _networkManager;

    private TextView _service;
    private Spinner _discoveredServices;
    private static Handler _discoverHandler;
    private SpinnerListen _discoverySelectListener;
    protected static NetworkConnect _connection;
    NsdServiceInfo _selectedServiceInfo = null;
    Context _context = this;

    public static final String TAG = "NetworkDetect";

    public class SpinnerListen implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String serviceSelected = parent.getItemAtPosition(position).toString();
            Log.d(TAG, "selected service is " + serviceSelected);

            for(NsdServiceInfo i : _networkManager._discoveredServices) {
                String name = i.getServiceName() + " " + i.getServiceType();
                if ( name.equals(serviceSelected) )
                {
                    _selectedServiceInfo = i;
                    break;
                }
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            _selectedServiceInfo = null;
        }
    }

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
        _discoverySelectListener = new SpinnerListen();
        _discoveredServices.setOnItemSelectedListener(_discoverySelectListener);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Starting ");
        _connection = new NetworkConnect();
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
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Being destroyed.");
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

    public void clickConnect(View view) {
        if (_connection._isServerConnected) {
            Context context = getApplicationContext();
            CharSequence text = "Client " + _connection.getHostName() + " connected";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            Intent intent = new Intent(this, PingActivity.class);

            startActivity(intent);
        }
        if (_selectedServiceInfo == null) {
            Context context = getApplicationContext();
            CharSequence text = "No service selected";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            _networkManager.stopDiscovery();
            _networkManager.resolveService(_selectedServiceInfo);
            _selectedServiceInfo = _networkManager.getChosenServiceInfo();
            Log.d(TAG, "connecting to " + _selectedServiceInfo.getServiceName());
            _connection.connectToServer(_selectedServiceInfo.getHost(), _selectedServiceInfo.getPort());

            Intent intent = new Intent(this, PingActivity.class);

            startActivity(intent);
        }
    }

}