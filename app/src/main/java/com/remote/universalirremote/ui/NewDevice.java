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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.remote.universalirremote.ApplicationWideSingleton;
import com.remote.universalirremote.Constant;
import com.remote.universalirremote.R;
import com.remote.universalirremote.database.DeviceData;
import com.remote.universalirremote.database.DeviceInfoRepository;

//
// Activity to add new device
//
// Usage:
//          Select Device protocol from Dropdown
//          Select Device Layout
//          Enter Name
//          Tap OK
//
//
//    Launch by:
//        DeviceSelect
//          intent:
//              passes:
//                  Launcher:           INT_LAUNCHER_KEY           : INT_LAUNCHER_DEVICE_SELECT (1)
//                  Blaster Address:    INT_SERVICE_KEY
//                          NsdServiceInfo retrieve using .getParcelable(KEY)
//
//    Launches:
//        multiple layouts not yet finalised
//        DeviceSelect
//                  on Back pressed.
//
//
public class NewDevice extends AppCompatActivity {

    DeviceInfoRepository _deviceDataRepository;

    private static final int _layoutDropdownId = R.id.spnr_DeviceSelect;
    private static final int _editTextName = R.id.editTextName;
    private static final int _protocolDropDownId = R.id.spnr_protocolSelect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_remote);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.support_simple_spinner_dropdown_item,
                new String[] {
                        Constant.NO_SELECT, Constant.Layout.AC_SPINNER, Constant.Layout.TV_SPINNER, Constant.Layout.GEN_SPINNER });

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner layoutUI = findViewById(_layoutDropdownId);
        layoutUI.setAdapter(adapter);
        layoutUI.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int layout = Constant.getLayout(
                        adapter.getItem(position)
                );
                if(layout == Constant.Layout.LAY_AC) {

                    ((Spinner) findViewById(_protocolDropDownId)).setVisibility(View.VISIBLE);

                } else {

                    ((Spinner) findViewById(_protocolDropDownId)).setVisibility(View.INVISIBLE);
                    ((Spinner)findViewById(_protocolDropDownId)).setSelection(Constant.Protocols.RAW);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ((Spinner) findViewById(_protocolDropDownId)).setVisibility(View.INVISIBLE);
                ((Spinner)findViewById(_protocolDropDownId)).setSelection(Constant.Protocols.RAW);
            }
        });

        ArrayAdapter<String> protocolAdapter = new ArrayAdapter<>(
                this, R.layout.support_simple_spinner_dropdown_item,
                Constant.Protocols._protocolList
        );
        Spinner protocolUI = findViewById(_protocolDropDownId);
        protocolUI.setAdapter(protocolAdapter);
        protocolUI.setVisibility(View.INVISIBLE);

        if(savedInstanceState != null) {
            NsdServiceInfo service = savedInstanceState.getParcelable(Constant.INT_SERVICE_KEY);

            ApplicationWideSingleton.refreshSelectedService(service);
        }
    }

    @Override
    protected void onResume() {

        ApplicationWideSingleton.refreshSelectedService(
                getIntent().getParcelableExtra(Constant.INT_SERVICE_KEY)
        );
        ((TextView)findViewById(R.id.text_selectedBlaster_add_remote))
                .setText(ApplicationWideSingleton.getSelectedService().getServiceName());
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putParcelable(Constant.INT_SERVICE_KEY, ApplicationWideSingleton.getSelectedService());
        super.onSaveInstanceState(outState);
    }

    // Menu item selected process
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void clickCreate(View view) {
        String name = ((EditText) findViewById(_editTextName)).getText().toString();
        String layout = ((Spinner)findViewById(_layoutDropdownId)).getSelectedItem().toString();
        String protocol = ((Spinner) findViewById(_protocolDropDownId)).getSelectedItem().toString();

        if(name == null || name.equals(Constant.NO_SELECT)) {
            Toast.makeText(getApplicationContext(), "No name selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if(protocol == null || protocol.equals(Constant.NO_SELECT)) {
            Toast.makeText(getApplicationContext(), "No protocol selected", Toast.LENGTH_LONG).show();
            return;
        }

        if(layout == null || layout.equals(Constant.NO_SELECT)) {
            Toast.makeText(getApplicationContext(), "No layout selected", Toast.LENGTH_LONG).show();
            return;
        }
        if(_deviceDataRepository.doesExist(name)) {
            Toast.makeText(getApplicationContext(), "Device name exists", Toast.LENGTH_LONG).show();
            return;
        }

        DeviceData device = new DeviceData(name, Constant.getLayout(layout), Constant.getProtocol(protocol));
        _deviceDataRepository.insert(device);

        ApplicationWideSingleton.setSelectedDevice(name);

        Intent intent = null;

        switch( device.getDeviceLayout() ) {
            case Constant.Layout.LAY_TV: {
                intent = new Intent(this, TvRemoteConfigure.class);
                break;
            }

            case Constant.Layout.LAY_AC: {
                intent = new Intent(this, AcRemote.class);
                break;
            }
            case Constant.Layout.LAY_GEN: {
                intent = new Intent(this, GenRemoteConfigure.class);
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

         intent.putExtra(Constant.INT_SELECTED_DEVICE, device.getDeviceName());
         startActivity(intent);
    }


}