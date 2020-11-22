package com.gectcr.ece.design.tutorial.networktest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.concurrent.CopyOnWriteArrayList;

public class DiscoverActivity extends AppCompatActivity {

    NetworkManager _networkManager;
    Handler _discoveryHandler;
    HandlerThread _discoveryThread;
    CopyOnWriteArrayList<String> _discoveredServices;
    private Spinner _discoveredServicesUIList;
    public static final String TAG = "DiscoverActivity";
    private NsdServiceInfo _selectedFromList = null;

    public class SpinnerListen implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String serviceSelected = parent.getItemAtPosition(position).toString();
            Log.d(TAG, "selected service is " + serviceSelected);
            CopyOnWriteArrayList<NsdServiceInfo> allServices = _networkManager.getDiscoveredServices();
            for(NsdServiceInfo i : allServices) {
                String name = i.getServiceName() + " " + i.getServiceType();
                if ( name.equals(serviceSelected) )
                {
                    _selectedFromList = i;
                    break;
                }
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            _selectedFromList = null;
        }
    }

    private SpinnerListen _discoveredServicesSpinnerListener;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        _discoveredServicesSpinnerListener = new SpinnerListen();
        _discoveredServices = new CopyOnWriteArrayList<String>();
        _discoveredServicesUIList = (Spinner) findViewById(R.id.allDiscoveredServices);
        _discoveryThread = new HandlerThread("DiscoverHandler");

        for (NsdServiceInfo i : _networkManager.getDiscoveredServices())
            _discoveredServices.add(i.getServiceName() + " " + i.getServiceType());

        _discoveryHandler = new Handler(_discoveryThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                Bundle msgBundle = msg.getData();
                String name = msgBundle.getString(NetworkManager.DISCOVERY_HANDLER_KEY);
                if(name == null) {
                    name = msgBundle.getString(NetworkManager.DISCOVERY_HANDLER_LOST_KEY);
                    _discoveredServices.remove(name);
                } else {
                    _discoveredServices.add(name);
                }
            }
        };
        _networkManager = new NetworkManager(this, _discoveryHandler);
    }

    public void clickConnect(View view ) {

    }
}