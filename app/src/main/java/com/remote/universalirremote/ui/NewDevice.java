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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.remote.universalirremote.ApplicationWideSingleton;
import com.remote.universalirremote.Constant;
import com.remote.universalirremote.R;
import com.remote.universalirremote.database.DeviceData;
import com.remote.universalirremote.database.DeviceDataParcelable;
import com.remote.universalirremote.database.DeviceInfoRepository;
import com.remote.universalirremote.network.NetworkErrorCallback;
import com.remote.universalirremote.network.RawGet;

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

    private DeviceInfoRepository _deviceDataRepository;
    private RawGet _getProtocolInfo;
    private Handler _getProtocolHandler;
    private HandlerThread _getProtocolHandlerThread;

    private static final int _layoutDropdownId = R.id.spnr_DeviceSelect;
    private static final int _editTextName = R.id.editTextName;
    private static final int _protocolDropDownId = R.id.spnr_protocolSelect;
    private static final int _modelDropDownId = R.id.spnr_modelSelect;
    public static final String TAG = "NewDevice";
    private final Context _context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_remote);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.support_simple_spinner_dropdown_item,
                new String[] {
                        Constant.NO_SELECT, Constant.Layout.AC_SPINNER, Constant.Layout.TV_SPINNER, Constant.Layout.GEN_SPINNER });
        ArrayAdapter<String> protocolAdapter = new ArrayAdapter<>(
                this, R.layout.support_simple_spinner_dropdown_item,
                Constant.Protocols._protocolList
        );
        Spinner protocolUI = findViewById(_protocolDropDownId);
        _getProtocolHandlerThread = new HandlerThread("protocolGet");
        _getProtocolHandlerThread.start();
        _getProtocolHandler = new Handler(_getProtocolHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                int protocol = msg.getData().getInt(RawGet.PROTOCOL_KEY);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(NewDevice.this);
                alertDialog.setTitle("Protocol AutoDetect");
                alertDialog.setMessage("Protocol " + Constant.getProtocol(protocol) + " detected, Use this?");

                alertDialog.setPositiveButton("OK",
                        (dialog, which) -> {
                            runOnUiThread(
                                    ()-> {
                                        protocolUI.setSelection(protocolAdapter.getPosition(Constant.getProtocol(protocol)));
                                    }
                            );
                            dialog.dismiss();
                        });

                alertDialog.setNegativeButton("Cancel",
                        (dialog, which) -> dialog.cancel());

                alertDialog.show();
            }
        };

        _getProtocolInfo = new RawGet(ApplicationWideSingleton.getSelectedService(), _getProtocolHandler, new NetworkErrorCallback() {
            @Override
            public void errorResponse(String errorString) {
                runOnUiThread(
                        () -> Toast.makeText(getApplicationContext(),
                                "Protocol auto detect failed due to network error",
                                Toast.LENGTH_SHORT).show()
                );
            }
        });

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

                    findViewById(_protocolDropDownId).setVisibility(View.VISIBLE);
                    _getProtocolInfo.getData(0);
                } else {

                    findViewById(_protocolDropDownId).setVisibility(View.INVISIBLE);
                    ((Spinner)findViewById(_protocolDropDownId)).setSelection(Constant.Protocols.RAW + 1);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                findViewById(_protocolDropDownId).setVisibility(View.INVISIBLE);
                ((Spinner)findViewById(_protocolDropDownId)).setSelection(Constant.Protocols.RAW + 1);
            }
        });


        protocolUI.setAdapter(protocolAdapter);
        protocolUI.setVisibility(View.INVISIBLE);

        ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(
                this, R.layout.support_simple_spinner_dropdown_item,
                new String[] { Constant.NO_SELECT }
        );
        Spinner modelUI = findViewById(_modelDropDownId);
        modelUI.setAdapter(modelAdapter);
        modelUI.setVisibility(View.INVISIBLE);

        protocolUI.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int protocol = Constant.getProtocol(
                        protocolAdapter.getItem(position)
                );
                Constant.ModelProtocolList modelList = Constant.getModelListForProtocol(protocol);
                if(modelList == null)
                    onNothingSelected(parent);
                else {
                    ArrayAdapter<String> model = new ArrayAdapter<>(
                            _context, R.layout.support_simple_spinner_dropdown_item,
                            modelList._modeName
                    );
                    modelUI.setAdapter(model);
                    modelUI.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ArrayAdapter<String> model = new ArrayAdapter<>(
                        _context, R.layout.support_simple_spinner_dropdown_item,
                        new String[] {Constant.NO_SELECT}
                );
                modelUI.setAdapter(model);
                modelUI.setVisibility(View.INVISIBLE);
            }
        });


        Intent intent = getIntent();
        if(intent != null) {
            Log.d(TAG, "intent called now saving");
            NsdServiceInfo service = intent.getParcelableExtra(Constant.INT_SERVICE_KEY);

            if(service != null )
                ApplicationWideSingleton.refreshSelectedService(service);
        } else if(savedInstanceState != null) {
            NsdServiceInfo service = savedInstanceState.getParcelable(Constant.INT_SERVICE_KEY);

            ApplicationWideSingleton.refreshSelectedService(service);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        _deviceDataRepository = new DeviceInfoRepository(getApplication(), null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationWideSingleton.refreshSelectedService(
                getIntent().getParcelableExtra(Constant.INT_SERVICE_KEY)
        );
        ((TextView)findViewById(R.id.text_selectedBlaster_add_remote))
                .setText(ApplicationWideSingleton.getSelectedService().getServiceName());

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constant.INT_SERVICE_KEY, ApplicationWideSingleton.getSelectedService());

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
        String model = ((Spinner) findViewById(_modelDropDownId)).getSelectedItem().toString();
        int modelId = 0;

        if(name == null || name.equals(Constant.NO_SELECT)) {
            Toast.makeText(getApplicationContext(), "No name selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if(protocol == null || protocol.equals(Constant.NO_SELECT)) {
            Toast.makeText(getApplicationContext(), "No protocol selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if(layout == null || layout.equals(Constant.NO_SELECT)) {
            Toast.makeText(getApplicationContext(), "No layout selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if(Constant.getModelListForProtocol(Constant.getProtocol(protocol)) != null) {
            if (model == null || model.equals(Constant.NO_SELECT)) {
                Toast.makeText(getApplicationContext(), "No model selected", Toast.LENGTH_SHORT).show();
                return;
            }
            modelId = Constant.getModelListForProtocol(Constant.getProtocol(protocol)).getModelId(model);
        }


        int finalModelId = modelId;
        _deviceDataRepository.useDatabaseExecutor(
                () -> {
                    if(_deviceDataRepository.getDao().doesDeviceExist(name)) {
                        runOnUiThread(
                                () -> Toast.makeText(getApplicationContext(),
                                        "Device name exists", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    DeviceData device = new DeviceData(name, Constant.getProtocol(protocol), Constant.getLayout(layout), finalModelId);
                    ApplicationWideSingleton.setSelectedDevice(device);

                    _deviceDataRepository.getDao().insert(device);


                    Intent intent;

                    switch( device.getDeviceLayout() ) {
                        case Constant.Layout.LAY_TV: {
                            intent = new Intent(_context, TvRemoteConfigure.class);
                            break;
                        }

                        case Constant.Layout.LAY_AC: {
                            intent = new Intent(_context, AcRemote.class);
                            break;
                        }
                        case Constant.Layout.LAY_GEN: {
                            intent = new Intent(_context, GenRemoteConfigure.class);
                            break;
                        }

                        default: {
                            runOnUiThread(
                                    () -> Toast.makeText(getApplicationContext(),
                                    "invalid layout", Toast.LENGTH_SHORT).show());
                            return;
                        }
                    }
                    intent.putExtra(Constant.INT_LAUNCHER_KEY, Constant.INT_LAUNCHER_DEVICE_SELECT);
                    intent.putExtra(Constant.INT_SERVICE_KEY,
                            (NsdServiceInfo)getIntent().getParcelableExtra(Constant.INT_SERVICE_KEY));

                    intent.putExtra(Constant.INT_SELECTED_DEVICE, new DeviceDataParcelable(device));
                    startActivity(intent);
                }
        );

    }


}