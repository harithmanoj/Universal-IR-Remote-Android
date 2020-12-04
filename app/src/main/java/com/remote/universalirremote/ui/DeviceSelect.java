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

package com.remote.universalirremote.ui;

import androidx.annotation.NonNull;
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

import com.remote.universalirremote.ApplicationWideSingleton;
import com.remote.universalirremote.Constant;
import com.remote.universalirremote.R;
import com.remote.universalirremote.database.DeviceData;
import com.remote.universalirremote.database.DeviceInfoRepository;

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
//        NewDevice
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

    private NsdServiceInfo _selectedService;

    private boolean setSelectedDevice(String deviceName) {
        if(deviceName.equals(Constant.NO_SELECT)) {
            _selectedDevice = null;
            return false;
        }

        if(_deviceDataRepository.doesExist(deviceName)) {
            Log.d(TAG, "selected device is " + deviceName);
            return setSelectedDevice(_deviceDataRepository.getDevice(deviceName));
        }
        else {
            _selectedDevice = null;
            return false;
        }
    }

    private boolean setSelectedDevice(DeviceData device) {
        _selectedDevice = device;
        if (device == null)
            return false;
        ApplicationWideSingleton.refreshSelectedDevice(device.getDeviceName());
        return true;
    }


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

                        if( setSelectedDevice(name) ) {
                            Log.d(TAG, "selected device is " + name);

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
                        TextView info = findViewById(R.id.text_selectedDeviceInfo);
                        info.setText("Nothing Selected");
                    }
                });

        String device = savedInstanceState.getString(Constant.INT_SELECTED_DEVICE);
        NsdServiceInfo service = savedInstanceState.getParcelable(Constant.INT_SERVICE_KEY);

        ApplicationWideSingleton.refreshSelectedDevice(device);
        ApplicationWideSingleton.refreshSelectedService(service);

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        if(ApplicationWideSingleton.isSelectedDeviceValid())
            outState.putString(Constant.INT_SELECTED_DEVICE, ApplicationWideSingleton.getSelectedDevice());
        outState.putParcelable(Constant.INT_SERVICE_KEY, ApplicationWideSingleton.getSelectedService());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {

        _selectedService = getIntent().getParcelableExtra(Constant.INT_SERVICE_KEY);
        ApplicationWideSingleton.refreshSelectedService(_selectedService);
        ((TextView)findViewById(R.id.text_selectedBlaster)).setText(_selectedService.getServiceName());
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            Intent intent;
            switch( _selectedDevice.getDeviceLayout() ) {
                case Constant.Layout.LAY_TV: {
                    intent = new Intent(this, TvRemoteTransmit.class);
                    break;
                }

                case Constant.Layout.LAY_AC: {
                    intent = new Intent(this, AcRemote.class);
                    break;
                }
                case Constant.Layout.LAY_GEN: {
                    intent = new Intent(this, GenRemoteTransmit.class);
                    break;
                }

                default: {
                    Toast.makeText(getApplicationContext(),
                            "invalid layout", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            intent.putExtra(Constant.INT_LAUNCHER_KEY, Constant.INT_LAUNCHER_DEVICE_SELECT);

            intent.putExtra(Constant.INT_SERVICE_KEY,
                    (NsdServiceInfo)getIntent().getParcelableExtra(Constant.INT_SERVICE_KEY));
            intent.putExtra(Constant.INT_SELECTED_DEVICE, _selectedDevice.getDeviceName());
            startActivity(intent);
        }
    }

    // Add new remote
    public void clickNew( View view ) {
        Intent intent = new Intent(this, NewDevice.class);
        intent.putExtra(Constant.INT_LAUNCHER_KEY, Constant.INT_LAUNCHER_DEVICE_SELECT);
        intent.putExtra(Constant.INT_SERVICE_KEY,
                (NsdServiceInfo)getIntent().getParcelableExtra(Constant.INT_SERVICE_KEY));
        startActivity(intent);
  }
}
