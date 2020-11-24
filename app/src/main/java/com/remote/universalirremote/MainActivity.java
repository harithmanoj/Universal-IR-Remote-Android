/*

        Copyright (C) 2020  Contributors (in contributors file)

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.remote.universalirremote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainActivity extends AppCompatActivity {

    // UI element which lists all discovered services.
    private Spinner _discoveredServicesUiList;

    // Selected Service Info
    private NsdServiceInfo _selectedService;

    // Discovery and resolution
    private NetworkManager _networkManager;

    // Thread for discovery handler to run on
    private HandlerThread _discoveryThread;

    // Handler to update spinner on discovery.
    private Handler _discoveryHandler;

    // List of names of all services.
    private ArrayAdapter<String> _discoveredServicesAdapter;

    // Debug TAG
    public static final String TAG = "MainActivity";

    // Spinner Content when no services are available.
    public static final String NO_SELECT = "None";

    // Getter for selected service
    private NsdServiceInfo getSelectedService() {
        return _selectedService;
    }

    // Set selected service
    // Returns false if service doesn't exist in _networkManger.getDiscoveredServices()
    // or if parameters are null
    private boolean setSelectedService(NsdServiceInfo service) {
        if ((service == null ) || (_networkManager == null) )
            return false;
        if (!_networkManager.getDiscoveredServices().contains(service)) {
            return false;
        } else {
            _selectedService = service;
            return true;
        }
    }

    // Set selected service ( take from _networkManger.getDiscoveredServices() with matching name).
    // Returns false if it doesn't exist or if parameters are null
    private boolean setSelectedService(String name) {
        if ((name == null ) || (_networkManager == null) )
            return false;
        NsdServiceInfo temp = _networkManager.getDiscoveredServices(name);
        if (temp == null) {
            return false;
        } else {
            _selectedService = temp;
            return true;
        }
    }

    // Refresh UI list of discovered services.
    public void refreshSpinner() {
        _discoveredServicesAdapter.clear();
        _discoveredServicesAdapter.add(NO_SELECT);
        CopyOnWriteArrayList<NsdServiceInfo> list = _networkManager.getDiscoveredServices();
        for ( NsdServiceInfo i : list) {
            _discoveredServicesAdapter.add(i.getServiceName()); // add all services
        }
        _discoveredServicesAdapter.notifyDataSetChanged(); // notify UI
    }

    // onCreate instantiation.
    // Initialized variables: _discoveredServicesAdapter
    //                        _discoveredServicesUiList
    //                        _discoveredServicesUiList.setOnItemSelectedListener(...)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _discoveredServicesAdapter = new ArrayAdapter<String>(
                this, R.layout.support_simple_spinner_dropdown_item,
                new String[] {NO_SELECT});
        _discoveredServicesUiList = (Spinner) findViewById(R.id.spnr_blasterSelection);
        _discoveredServicesUiList.setAdapter(_discoveredServicesAdapter);
        _discoveredServicesUiList.setOnItemSelectedListener
                (new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String name = parent.getItemAtPosition(position).toString();

                        if(name.equals(NO_SELECT))
                            onNothingSelected(parent);

                        Log.d(TAG, "selected service is " + name);
                        if (!setSelectedService(name)) {
                            Toast.makeText(getApplicationContext(),
                                    "service " + name + " not found",
                                    Toast.LENGTH_SHORT).show();
                            if (_discoveryHandler != null) {
                                Bundle msgBundle = new Bundle();
                                msgBundle.putInt(NetworkManager.DISCOVER_OP, NetworkManager.DISCOVER_REFRESH);
                                Message msg = new Message();
                                msg.setData(msgBundle);
                                _discoveryHandler.sendMessage(msg);
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        _selectedService = null;
                    }
                });
    }

    // Variables instantiated :     _discoveryThread
    //                              _discoveryHandler
    //                              _networkManager
    // [ starts discovery ]
    @Override
    protected void onStart() {
        Log.d(TAG, "Starting ");
        _discoveryThread = new HandlerThread("DiscoverHandler");
        _discoveryThread.start();

        _discoveryHandler = new Handler(_discoveryThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                Bundle msgBundle = msg.getData();

                int op = msgBundle.getInt(NetworkManager.DISCOVER_OP);

                switch(op) {
                    case NetworkManager.DISCOVER_NEW: { // add new service
                        _discoveredServicesAdapter.add(
                                msgBundle.getString(NetworkManager.DISCOVERED_SERVICE_NAME));
                        _discoveredServicesAdapter.notifyDataSetChanged();
                    }

                    case NetworkManager.DISCOVER_LOST: { // remove service
                        _discoveredServicesAdapter.remove(
                                msgBundle.getString(NetworkManager.DISCOVERED_SERVICE_NAME));
                        _discoveredServicesAdapter.notifyDataSetChanged();
                    }

                    case NetworkManager.DISCOVER_REFRESH: { // refresh UI
                        refreshSpinner();
                    }
                }

            }
        };
        _networkManager = new NetworkManager(this, _discoveryHandler);
        refreshSpinner();
        _networkManager.discoverServices();     // start discovery
        super.onStart();
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "Resuming");
        if (_networkManager != null) {
            _networkManager.discoverServices();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Pausing");
        if(_networkManager != null)
            _networkManager.stopDiscover();
        super.onPause();
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
}
