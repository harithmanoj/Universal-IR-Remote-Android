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
//

package com.remote.universalirremote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

//
// Activity to select device from existing
//
// Usage:
//          Select Device from Dropdown
//          Tap OK
//      OR
//          Tap New ( + )
//
//
//    Launch by:
//        MainActivity
//        intent:
//            passes:
//                Launcher:           INT_LAUNCHER_KEY           : INT_LAUNCHER_MAIN (0)
//                Blaster Address:    INT_SERVICE_KEY
//                        NsdServiceInfo retrieve using .getParcelable(KEY)
//
//    Launches:
//        multiple layouts not yet finalised
//        MainActivity
//                  on Back pressed.
//        AddRemote
//        intent:
//            passes:
//                Launcher:           INT_LAUNCHER_KEY           : INT_LAUNCHER_DEVICE_SELECT (1)
//                Blaster Address:    INT_SERVICE_KEY
//                        NsdServiceInfo retrieve using .getParcelable(KEY)
//
public class DeviceSelect extends AppCompatActivity {

    // Repo for DeviceData table
    private DeviceInfoRepository _deviceDataRepository;

    // debug TAG
    private static final String TAG = "DeviceSelect";

    // Selected device
    private DeviceData _selectedDevice = null;

    // onCreate instantiation.
    // Initialized variables: _deviceDataRepository
    //                        set back button
    //                        UI devices drop down list ( populate from database )
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_select);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        _deviceDataRepository = new DeviceInfoRepository(getApplication());

        List<String> deviceNames = _deviceDataRepository.getNames();

        deviceNames.add(0, Constant.NO_SELECT);

        ArrayAdapter<String> deviceListAdapter = new ArrayAdapter<>(
                this, R.layout.support_simple_spinner_dropdown_item,
                deviceNames);
        deviceListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner devicesUiList = (Spinner) findViewById(R.id.spnr_DeviceSelection);
        devicesUiList.setAdapter(deviceListAdapter);

        devicesUiList.setOnItemSelectedListener
                (new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String name = parent.getItemAtPosition(position).toString();

                        if(name.equals(Constant.NO_SELECT))
                            onNothingSelected(parent);

                        Log.d(TAG, "selected device is " + name);
                        if(_deviceDataRepository.doesExist(name)) {
                            _selectedDevice = _deviceDataRepository.getDevice(name);
                            TextView info = findViewById(R.id.text_selectedDeviceInfo);

                            String layout = Constant.getLayout(_selectedDevice.getDeviceLayout());

                            info.setText(
                                    new StringBuilder().append("Device : ")
                                            .append(_selectedDevice.getDeviceName())
                                            .append(" type ").append(layout).append("\n")
                                            .append("protocol used : ")
                                            .append(Constant.getProtocol(
                                                    _selectedDevice.getProtocolInfo())).toString()
                            );

                        }
                        else
                            onNothingSelected(parent);


                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        _selectedDevice = null;
                    }
                });

    }

    // Menu item selected process
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // click OK, launch selected remote
    public void clickOk(View view) {

        if(_selectedDevice == null) {
            Toast.makeText(getApplicationContext(),
                    "Select a device ", Toast.LENGTH_LONG).show();
        } else {
//            Intent intent;
//            switch( device.getDeviceLayout() ) {
//                case Constant.Layout.LAY_TV: {
//                    intent = new Intent(this, /* TV layout remote activity */);
//                }
//
//                case Constant.Layout.LAY_AC: {
//                    intent = new Intent(this, /* AC layout remote activity */);
//                }
//
//                default: {
//                    Toast.makeText(getApplicationContext(),
//                            "invalid layout", Toast.LENGTH_LONG).show();
//                    return;
//                }
//            }
//
//            intent.putExtra(Constant.INT_LAUNCHER_KEY, Constant.INT_LAUNCHER_DEVICE_SELECT);
//            intent.putExtra(Constant.INT_SERVICE_KEY,
//                    (NsdServiceInfo)getIntent().getParcelableExtra(Constant.INT_SERVICE_KEY))
//            intent.putExtra(Constant.INT_SELECTED_DEVICE, device.getDeviceName());
//            startActivity(intent);
        }
    }

    // Add new remote
    public void clickNew( View view ) {
        Intent intent = new Intent(this, AddRemote.class);
        intent.putExtra(Constant.INT_LAUNCHER_KEY, Constant.INT_LAUNCHER_DEVICE_SELECT);
        intent.putExtra(Constant.INT_SERVICE_KEY,
                (NsdServiceInfo)getIntent().getParcelableExtra(Constant.INT_SERVICE_KEY));
        startActivity(intent);
  }
}
