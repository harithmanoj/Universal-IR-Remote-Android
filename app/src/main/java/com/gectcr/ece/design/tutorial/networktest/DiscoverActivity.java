package com.gectcr.ece.design.tutorial.networktest;

import androidx.appcompat.app.AppCompatActivity;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.concurrent.CopyOnWriteArrayList;

public class DiscoverActivity extends AppCompatActivity {

    NetworkManager _networkManager;
    Handler _discoveryHandler;
    CopyOnWriteArrayList<CharSequence> _discoveredServices;
    private Spinner _discoveredServicesUIList;
    public static final String TAG = "DiscoverActivity";
    private NsdServiceInfo _selectedFromList;

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
        
    }
}