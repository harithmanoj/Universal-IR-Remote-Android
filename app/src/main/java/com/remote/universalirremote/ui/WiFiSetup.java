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


package com.remote.universalirremote.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.remote.universalirremote.Constant;
import com.remote.universalirremote.R;
import com.remote.universalirremote.network.WifiConfigure;

import java.util.ArrayList;

public class WiFiSetup extends AppCompatActivity {
    private WiFiSetup _configurationManager;

    private ArrayAdapter<String> _wifiScanAdapter;

    private Handler _scanHandler;

    private HandlerThread _scanHandlerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_setup);

        ArrayList<String> servicesList = new ArrayList<>();
        servicesList.add(Constant.NO_SELECT);
        _wifiScanAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, servicesList);
        _wifiScanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner)findViewById(R.id.spnr_wifiScan)).setAdapter(_wifiScanAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        _scanHandlerThread = new HandlerThread("Scan Handler");
        _scanHandlerThread.start();

        _scanHandler = new Handler(_scanHandlerThread.getLooper()) {
            public void HandleMessage(@NonNull Message msg) {
                _wifiScanAdapter.clear();

                String[] ssids = msg.getData().getStringArray(WifiConfigure.SCAN_KEY);

                _wifiScanAdapter.addAll(ssids);
            }
        };
    }

    public void clickRefresh() {

    }
}