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

package com.remote.universalirremote.ui;

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
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.remote.universalirremote.ApplicationWideSingleton;
import com.remote.universalirremote.Constant;
import com.remote.universalirremote.R;
import com.remote.universalirremote.network.NetworkManager;

import java.util.ArrayList;
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
//                        NsdServiceInfo retrieve using .getParcelable(KEY)
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

    // Adapter to interface with the discovery spinner
    private ArrayAdapter<String> _discoveredServicesAdapter;

    // Debug TAG
    public static final String TAG = "MainActivity";

    private void terminate() {
        _discoveryThread.quitSafely();
    }


    // Set selected service ( take from _networkManger.getDiscoveredServices() with matching name).
    // Returns false if it doesn't exist or if parameters are null
    private boolean setSelectedService(String name, boolean updateSingleton) {
        if ((name == null ) || (_networkManager == null) )
            return false;
        if (name.equals(Constant.NO_SELECT)) {
            _selectedService = null;
            return false;
        }
        return setSelectedService(_networkManager.getDiscoveredServices(name), updateSingleton);
    }

    private boolean setSelectedService(NsdServiceInfo service, boolean updateSingleton ) {
        if(service == null) {
            return false;
        }

        _selectedService = service;
        if(updateSingleton)
            ApplicationWideSingleton.setSelectedService(service);
        return true;
    }

    // Refresh UI list of discovered services.
    public void refreshSpinner() {
        _discoveredServicesAdapter.clear();
        ArrayList<String> servicesList = new ArrayList<>();
        servicesList.add(Constant.NO_SELECT);
        CopyOnWriteArrayList<NsdServiceInfo> list = _networkManager.getDiscoveredServices();
        for ( NsdServiceInfo i : list) {
            servicesList.add(i.getServiceName()); // add all services
        }
        _discoveredServicesAdapter.addAll(servicesList);
    }

    // onCreate instantiation.
    // Initialized variables: _discoveredServicesAdapter
    //                        _discoveredServicesUiList
    //                        _discoveredServicesUiList.setOnItemSelectedListener(...)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArrayList<String> servicesList = new ArrayList<>();
        servicesList.add(Constant.NO_SELECT);
		_discoveredServicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, servicesList);
		_discoveredServicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner)findViewById(R.id.spnr_blasterSelection)).setAdapter(_discoveredServicesAdapter);

        if(savedInstanceState != null) {

            NsdServiceInfo service = savedInstanceState.getParcelable(Constant.INT_SERVICE_KEY);

            ApplicationWideSingleton.refreshSelectedService(service);
        }
    }

    // Variables instantiated :     _discoveryThread
    //                              _discoveryHandler
    //                              _networkManager
    // [ starts discovery ]
    @Override
    protected void onStart() {
        super.onStart();
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
                        runOnUiThread(() -> {
                            _discoveredServicesAdapter.add(
                                    msgBundle.getString(NetworkManager.DISCOVERED_SERVICE_NAME));
                            _discoveredServicesAdapter.notifyDataSetChanged();
                        });
                        break;
                    }

                    case NetworkManager.DISCOVER_LOST: { // remove service
                        runOnUiThread(() -> {
                            _discoveredServicesAdapter.remove(
                                    msgBundle.getString(NetworkManager.DISCOVERED_SERVICE_NAME));
                            _discoveredServicesAdapter.notifyDataSetChanged();
                        });
                        break;
                    }

                    case NetworkManager.DISCOVER_REFRESH: { // refresh UI
                        runOnUiThread(() -> refreshSpinner());
                        break;
                    }
                }

            }
        };
        _networkManager = new NetworkManager(this, _discoveryHandler);
        refreshSpinner();
        _networkManager.discoverServices();     // start discovery
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(ApplicationWideSingleton.isSelectedDeviceValid())
            outState.putString(Constant.INT_SELECTED_DEVICE, ApplicationWideSingleton.getSelectedDevice().getDeviceName());
        if(ApplicationWideSingleton.isSelectedServiceValid())
            outState.putParcelable(Constant.INT_SERVICE_KEY, ApplicationWideSingleton.getSelectedService());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resuming");
        if (_networkManager != null) {
            _networkManager.discoverServices();
        }
        ProgressBar circle = findViewById(R.id.prg_resolveProgress);
        circle.setVisibility(View.GONE);
        ApplicationWideSingleton.refreshSelectedService(_selectedService);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Pausing");
        if(_networkManager != null)
            _networkManager.stopDiscover();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "stopped");
        terminate();
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

        long pos = ((Spinner)findViewById(R.id.spnr_blasterSelection)).getSelectedItemPosition();
        String name = _discoveredServicesAdapter.getItem((int)pos);
        if (!setSelectedService(name, false)) {
            if (_selectedService == null) {
                Toast.makeText(getApplicationContext(),
                        "No service Selected", Toast.LENGTH_SHORT).show();
            }
            else {
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

        if (_selectedService == null) {
            Toast.makeText(getApplicationContext(),
                    "No service Selected", Toast.LENGTH_SHORT).show();
        } else {
            _networkManager.stopDiscover();
            _networkManager.resolveServices(_selectedService);

            ProgressBar circle = findViewById(R.id.prg_resolveProgress);
            circle.setVisibility(View.VISIBLE);
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
                }

            }
            circle.setVisibility(View.GONE);
            if(_networkManager.getChosenServiceInfo() == null) {
                Toast.makeText(getApplicationContext(),
                        "cannot resolve " + _selectedService.getServiceName(),
                        Toast.LENGTH_SHORT).show();
                _networkManager._isResolved = false;
                setSelectedService(Constant.NO_SELECT, false);
                ((Spinner)findViewById(R.id.spnr_blasterSelection)).setSelection(0);
                return;
            }
            setSelectedService(_networkManager.getChosenServiceInfo(), true);
            Toast.makeText(getApplicationContext(),
                    _selectedService.getServiceName() + " resolved",
                    Toast.LENGTH_SHORT).show();
            Intent intent;
            if(getIntent().getAction() == null ){
                Intent incomingIntent = getIntent();

                boolean def = false;

                switch (incomingIntent.getIntExtra(Constant.INT_LAUNCHER_KEY, -2)) {
                    case Constant.INT_LAUNCHER_DEVICE_SELECT: {
                        intent = new Intent(this, DeviceSelect.class);
                        break;
                    }
                    case Constant.INT_LAUNCHER_NEW_DEVICE: {
                        intent = new Intent(this, NewDevice.class);
                        break;
                    }
                    case Constant.INT_LAUNCHER_AC_REMOTE: {
                        intent = new Intent(this, AcRemote.class);
                        break;
                    }
                    case Constant.INT_LAUNCHER_GEN_CONFIGURE: {
                        intent = new Intent(this, GenRemoteConfigure.class);
                        break;
                    }
                    case Constant.INT_LAUNCHER_GEN_TRANSMIT: {
                        intent = new Intent(this, GenRemoteTransmit.class);
                        break;
                    }
                    case Constant.INT_LAUNCHER_TV_CONFIGURE: {
                        intent = new Intent(this, TvRemoteConfigure.class);
                        break;
                    }
                    case Constant.INT_LAUNCHER_TV_TRANSMIT: {
                        intent = new Intent(this, TvRemoteTransmit.class);
                        break;
                    }
                    default: {
                        def = true;
                        intent = new Intent(this, DeviceSelect.class);
                        break;
                    }
                }

                if(!def) {
                    intent.putExtra(Constant.INT_SELECTED_DEVICE,
                            incomingIntent.getStringExtra(Constant.INT_SELECTED_DEVICE));
                }

            } else {
                intent = new Intent(this, DeviceSelect.class);
            }
            intent.putExtra(Constant.INT_LAUNCHER_KEY, Constant.INT_LAUNCHER_MAIN);
            intent.putExtra(Constant.INT_SERVICE_KEY, _selectedService);
            startActivity(intent);
        }
    }
}
