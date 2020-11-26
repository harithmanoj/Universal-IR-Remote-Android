//
//
//        Copyright (C) 2020  Contributors (in contributors file)
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with this program.  If not, see <https://www.gnu.org/licenses/>.
//

package com.remote.universalirremote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.concurrent.CopyOnWriteArrayList;



//
//    Class for backend of Main (Launcher) activity.
//
//    Discovery and resolution of IR Blaster is done here
//
//    Usage:
//        Select IR Blaster from drop down menu
//        tap OK
//        wait for resolution
//
//    Launch by:
//        any other activity if service is lost
//        intent:
//            pass any data required as a Bundle
//            declare constant in MainActivity to identify that activity and pass that code
//
//    Launches:
//        DeviceSelect
//        intent:
//            passes:
//                Launcher:           INT_LAUNCHER_KEY           : INT_LAUNCHER_MAIN (0)
//                Blaster Address:    INT_SERVICE_KEY
//                        NsdServiceInfo retrieve using getExtra().getParcelable(KEY)
//
//

public class MainActivity extends AppCompatActivity {

    // Selected Service Info
    private NsdServiceInfo _selectedService = null;

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
        _discoveredServicesAdapter.add(Constant.NO_SELECT);
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

        _discoveredServicesAdapter = new ArrayAdapter<>(
                this, R.layout.support_simple_spinner_dropdown_item,
                new String[] {Constant.NO_SELECT});
        Spinner discoveredServicesUiList = (Spinner) findViewById(R.id.spnr_blasterSelection);
        discoveredServicesUiList.setAdapter(_discoveredServicesAdapter);
        discoveredServicesUiList.setOnItemSelectedListener
                (new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String name = parent.getItemAtPosition(position).toString();

                        if(name.equals(Constant.NO_SELECT))
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
        ProgressBar circle = (ProgressBar) findViewById(R.id.prg_resolveProgress);
        circle.setVisibility(View.GONE);
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

    // Resolves Selected service and acquires address and port
    // executes on clicking OK button
    // Waits on resolve synchronisation
    // On success ( a service is selected, the next activity is launched and port,
    // address of service is passed
    // On fail, a toast will inform user of resolve fail and exit function.
    public void clickConnect( View view ) {

        if (_selectedService == null) {
            Toast.makeText(getApplicationContext(),
                    "No service Selected", Toast.LENGTH_LONG).show();
        } else {
            _networkManager.stopDiscover();
            _networkManager.resolveServices(_selectedService);

            ProgressBar circle = (ProgressBar) findViewById(R.id.prg_resolveProgress);
            circle.setVisibility(View.VISIBLE);
            _selectedService = _networkManager.getChosenServiceInfo();
            Toast.makeText(getApplicationContext(),
                    "resolving " + _selectedService.getServiceName(),
                    Toast.LENGTH_SHORT).show();
            synchronized (_networkManager._waitForResolution) {

                try {
                    while(!_networkManager._isResolved) {
                        _networkManager._waitForResolution.wait();
                    }

                } catch (InterruptedException ie) {
                    Log.e(TAG, "interrupted Exception ", ie);
                    ie.printStackTrace();
                }

            }
            circle.setVisibility(View.GONE);
            if(_networkManager.getChosenServiceInfo() == null) {
                Toast.makeText(getApplicationContext(),
                        "cannot resolve " + _selectedService.getServiceName(),
                        Toast.LENGTH_LONG).show();
                _networkManager._isResolved = false;
                return;
            }
            _selectedService = _networkManager.getChosenServiceInfo();
            Toast.makeText(getApplicationContext(),
                    _selectedService.getServiceName() + " resolved",
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, DeviceSelect.class );
            intent.putExtra(Constant.INT_LAUNCHER_KEY,Constant.INT_LAUNCHER_MAIN);
            intent.putExtra(Constant.INT_SERVICE_KEY, _selectedService);
            startActivity(intent);
        }
    }
}
