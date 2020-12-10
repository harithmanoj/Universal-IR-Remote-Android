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

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NavUtils;

import com.remote.universalirremote.ApplicationWideSingleton;
import com.remote.universalirremote.GenRemote;
import com.remote.universalirremote.R;
import com.remote.universalirremote.database.DeviceButtonConfig;
import com.remote.universalirremote.network.RawGet;

import java.util.ArrayList;

public class GenRemoteConfigure extends GenRemote {

    private RawGet _getRawIrTiming;
    private HandlerThread _getResponseHandlerThread;
    private Handler _getResponseHandler;
    private ArrayList<DeviceButtonConfig> _allButtons;

    private boolean _hasCompletedSave;
    private final Object _waitOnWriteCompletion = new Object();

    public static final String USE_MOD = "handler.mode";
    public static final int STORE_ALL = 3;
    public static final int SET_NAME = 4;
    public static final String SET_NAME_KEY = "handler.key";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        _allButtons = new ArrayList<>();
        super.onCreate(savedInstanceState);
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

    @Override
    protected void onStart() {
        _deviceButtonConfigRepo.useDatabaseExecutor(
                () -> {
                    _buttonConfigList = _deviceButtonConfigRepo
                            .getDao().getAllRawData(
                                    ApplicationWideSingleton.getSelectedDeviceName());
                    runOnUiThread(
                            () -> {
                                setDisplayName(R.id.btn_2, lookupButton(BTN_GEN_2));
                                setDisplayName(R.id.btn_3, lookupButton(BTN_GEN_3));
                                setDisplayName(R.id.btn_4, lookupButton(BTN_GEN_4));
                                setDisplayName(R.id.btn_5, lookupButton(BTN_GEN_5));
                                setDisplayName(R.id.btn_6, lookupButton(BTN_GEN_6));
                                setDisplayName(R.id.btn_7, lookupButton(BTN_GEN_7));
                                setDisplayName(R.id.btn_8, lookupButton(BTN_GEN_8));
                                setDisplayName(R.id.btn_9, lookupButton(BTN_GEN_9));
                                setDisplayName(R.id.btn_10, lookupButton(BTN_GEN_10));
                                setDisplayName(R.id.btn_11, lookupButton(BTN_GEN_11));
                                setDisplayName(R.id.btn_12, lookupButton(BTN_GEN_12));
                                setDisplayName(R.id.btn_13, lookupButton(BTN_GEN_13));
                                if (!setDisplayName(TEXT_GEN_A, lookupButton(BTN_GEN_A_UP)))
                                    setDisplayName(TEXT_GEN_A, lookupButton(BTN_GEN_A_DOWN));
                                if (!setDisplayName(TEXT_GEN_X, lookupButton(BTN_GEN_X_UP)))
                                    setDisplayName(TEXT_GEN_X, lookupButton(BTN_GEN_X_DOWN));
                            }
                    );
                }
        );

        _getResponseHandlerThread = new HandlerThread("RawGenRemoteGetResponse");
        _getResponseHandlerThread.start();
        _hasCompletedSave = false;
        _getResponseHandler = new Handler(_getResponseHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                int mode = msg.getData().getInt(USE_MOD);
                int id = msg.getData().getInt(RawGet.BUTTON_ID_KEY);
                if(mode == 0) {
                    for (int i = 0; i < _allButtons.size(); ++i) {
                        if (_allButtons.get(i).getButtonId() == id) {
                            DeviceButtonConfig current = _allButtons.get(i);
                            if(current.getDeviceButtonName() != null) {
                                _allButtons.set(i,
                                        new DeviceButtonConfig(
                                                current.getButtonId(),
                                                msg.getData().getString(RawGet.RAW_KEY),
                                                current.getDeviceName(),
                                                current.isEditableName(),
                                                current.getDeviceButtonName()
                                        )
                                );
                            } else {
                                _allButtons.set(i,
                                        new DeviceButtonConfig(
                                                current.getButtonId(),
                                                msg.getData().getString(RawGet.RAW_KEY),
                                                current.getDeviceName(),
                                                current.isEditableName(),
                                                "##"
                                        )
                                );
                            }

                            runOnUiThread(
                                    () -> Toast.makeText(
                                            getApplicationContext(),
                                            "button " + current.getDeviceButtonName() + " configured",
                                            Toast.LENGTH_LONG
                                    ).show()
                            );
                            break;
                        }
                    }
                } else if (mode == SET_NAME) {
                    for (int i = 0; i < _allButtons.size(); ++i) {
                        if (_allButtons.get(i).getButtonId() == id) {
                            DeviceButtonConfig current = _allButtons.get(i);
                            _allButtons.set(i,
                                    new DeviceButtonConfig(
                                            current.getButtonId(),
                                            current.getIrTimingData(),
                                            current.getDeviceName(),
                                            current.isEditableName(),
                                            msg.getData().getString(SET_NAME_KEY)
                                    )
                            );
                            runOnUiThread(
                                    () -> {
                                        setDisplayName(current.getButtonId(),current);
                                        Toast.makeText(
                                                getApplicationContext(),
                                                "set name " + current.getDeviceButtonName(),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                            );

                        }
                    }
                } else if (mode == STORE_ALL) {
                    synchronized (_waitOnWriteCompletion) {
                        for (DeviceButtonConfig i : _allButtons) {
                            if(i.getIrTimingData() != null ) {
                                if(_deviceButtonConfigRepo.getDao()
                                        .doesExist(i.getDeviceName(),
                                                i.getButtonId())) {
                                    _deviceButtonConfigRepo.getDao().update(i);
                                } else {
                                    _deviceButtonConfigRepo.getDao().insert(i);
                                }
                            }
                        }
                        _allButtons.clear();
                        _hasCompletedSave = true;
                        _waitOnWriteCompletion.notifyAll();
                    }
                }
            }
        };
        _getRawIrTiming = new RawGet(ApplicationWideSingleton.getSelectedService(),
                _getResponseHandler);
        super.onStart();
    }


    private void setName(int btnId, String Name) {
        int index = -1;
        for (int i = 0; i <_allButtons.size(); ++i)
            if (_allButtons.get(i).getButtonId() == btnId) {
                index = i;
                break;
            }
        if(index == -1) {
            _allButtons.add(
                    new DeviceButtonConfig(
                            btnId,
                            null,
                            ApplicationWideSingleton.getSelectedDeviceName(),
                            true,
                            Name
                    )
            );

        } else {
            DeviceButtonConfig current = _allButtons.get(index);
            _allButtons.set(index,
                    new DeviceButtonConfig(
                            current.getButtonId(),
                            current.getIrTimingData(),
                            current.getDeviceName(),
                            true,
                            Name
                    ));
        }
        runOnUiThread(
                () -> {
                    setDisplayName(btnId,Name);
                    Toast.makeText(
                            getApplicationContext(),
                            "set name " + Name,
                            Toast.LENGTH_LONG
                    ).show();
                }
        );
    }

    @Override
    protected void onResume() {
        renameOkOrConfig("OK");
        _deviceButtonConfigRepo.getAllRawData(ApplicationWideSingleton.getSelectedDeviceName());
        super.onResume();
    }

    @Override
    public void handleButtonClicks(int btnId) {
        _deviceButtonConfigRepo.useDatabaseExecutor(
                () -> {
                    DeviceButtonConfig current = null;
                    boolean found = false;
                    for (int i = 0; i < _allButtons.size(); ++i) {
                        if (_allButtons.get(i).getButtonId() == btnId) {
                            current = _allButtons.get(i);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        if (_deviceButtonConfigRepo.getDao()
                                .doesExist(ApplicationWideSingleton
                                        .getSelectedDeviceName(), btnId)) {
                            current = _deviceButtonConfigRepo.getDao()
                                    .getButtonConfig(
                                            ApplicationWideSingleton.getSelectedDeviceName(), btnId);
                        } else {

                            boolean editable = true;
                            String name = "##";

                            if (btnId == BTN_GEN_DOWN) {
                                editable = false;
                                name = "BTN_GEN_DOWN";
                            } else if (btnId == BTN_GEN_UP) {
                                editable = false;
                                name = "BTN_GEN_UP";
                            } else if (btnId == BTN_GEN_LEFT) {
                                editable = false;
                                name = "BTN_GEN_LEFT";
                            } else if (btnId == BTN_GEN_RIGHT) {
                                editable = false;
                                name = "BTN_GEN_RIGHT";
                            } else if (btnId == BTN_GEN_OK) {
                                editable = false;
                                name = "BTN_GEN_OK";
                            } else if (btnId == BTN_GEN_POWER) {
                                editable = false;
                                name = "BTN_GEN_POWER";
                            }


                            current = new DeviceButtonConfig(
                                    btnId,
                                    null,
                                    ApplicationWideSingleton.getSelectedDeviceName(),
                                    editable,
                                    name
                            );
                        }
                    }
                    _allButtons.add(
                            new DeviceButtonConfig(
                                    current.getButtonId(),
                                    null,
                                    current.getDeviceName(),
                                    current.isEditableName(),
                                    current.getDeviceButtonName()
                            )
                    );
                    synchronized (_waitOnWriteCompletion) {
                        _hasCompletedSave = false;
                    }
                }
        );
        _getRawIrTiming.getData(btnId);
    }

    @Override
    public void startTransitOrConfigActivity(Intent configIntent, Intent transmitIntent) {
        Bundle msgBundle = new Bundle();
        msgBundle.putInt(USE_MOD, STORE_ALL);
        Message msg = new Message();
        msg.setData(msgBundle);
        _getResponseHandler.sendMessage(msg);
        ProgressDialog dialog = new ProgressDialog(this); // this = YourActivity
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Writing");
        dialog.setMessage("Writing configuration to database, please wait");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        synchronized (_waitOnWriteCompletion) {
            try {
                while (!_hasCompletedSave)
                    _waitOnWriteCompletion.wait();
            } catch (InterruptedException ex) {
                Log.d(TAG, "interrupted ", ex);
                ex.printStackTrace();
            }
        }
        dialog.dismiss();
        startActivity(transmitIntent);
    }
}
