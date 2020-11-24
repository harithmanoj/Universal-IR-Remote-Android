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

import androidx.appcompat.app.AppCompatActivity;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

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

    public static final String TAG = "MainActivity";

    private NsdServiceInfo getSelectedService() {
        return _selectedService;
    }

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

    private boolean setSelectedService(String name) {
        if ((name == null ) || (_networkManager == null) )
            return false;
        NsdServiceInfo temp = _networkManager.getDiscoveredService(name);
        if (temp == null) {
            return false;
        } else {
            _selectedService = temp;
            return true;
        }
    }

    public void refreshSpinner() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _discoveredServicesUiList = (Spinner) findViewById(R.id.spnr_blasterSelection);
        _discoveredServicesUiList.setOnItemSelectedListener
                (new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String name = parent.getItemAtPosition(position).toString();
                        Log.d(TAG, "selected service is " + name);
                        if (!setSelectedService(name)) {
                            Toast.makeText(getApplicationContext(),
                                    "service " + name + " not found",
                                    Toast.LENGTH_SHORT).show();
                            refreshSpinner();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        _selectedService = null;
                    }
                });
    }
}
