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
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.concurrent.ArrayBlockingQueue;

public class MainActivity extends AppCompatActivity {

    // UI element which lists all discovered services.
    private Spinner _discoveredServicesUiList;

    // List of all discovered Services.
    private ArrayBlockingQueue<String> _discoveredServices;


    // Selected Service Info
    private NsdServiceInfo _selectedService;

    // Discovery and resolution
    private NetworkManager _networkManager;

    // Thread for discovery handler to run on
    private HandlerThread _discoveryThread;

    // Handler to update spinner on discovery.
    private Handler _discoveryHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }
}
