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
import com.remote.universalirremote.TvRemote;
import com.remote.universalirremote.database.DeviceButtonConfig;
import com.remote.universalirremote.network.RawGet;

import java.util.ArrayList;

public class TvRemoteConfigure extends TvRemote {

    private RawGet _getRawIrTiming;
    private HandlerThread _getResponseHandlerThread;
    private ArrayList<DeviceButtonConfig> _addedButtons;
    private ArrayList<DeviceButtonConfig> _configuredButtons;
    private Handler _getResponseHandler;

    private boolean _hasCompletedSave;
    private final Object _waitOnWriteCompletion = new Object();

    public static final String USE_MOD = "handler.mode";
    public static final int STORE_ALL = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        _addedButtons = new ArrayList<>();
        _configuredButtons = new ArrayList<>();
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
        _getResponseHandlerThread = new HandlerThread("RawTvRemoteGetResponse");
        _getResponseHandlerThread.start();
        _hasCompletedSave = false;
        _getResponseHandler = new Handler(_getResponseHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                int mode = msg.getData().getInt(USE_MOD);
                if(mode == 0) {

                    int id = msg.getData().getInt(RawGet.BUTTON_ID_KEY);
                    for (int i = 0; i < _addedButtons.size(); ++i) {
                        if (_addedButtons.get(i).getButtonId() == id) {
                            DeviceButtonConfig current = _addedButtons.get(i);
                            _configuredButtons.add(
                                    new DeviceButtonConfig(
                                            id,
                                            msg.getData().getString(RawGet.RAW_KEY),
                                            current.getDeviceName(),
                                            current.isEditableName(),
                                            current.getDeviceButtonName()
                                    )
                            );

                            runOnUiThread(
                                    () -> Toast.makeText(
                                            getApplicationContext(),
                                            "button " + current.getDeviceButtonName() + " configured",
                                            Toast.LENGTH_SHORT).show()
                            );

                            break;
                        }
                    }
                    _addedButtons.clear();
                } else if (mode == STORE_ALL) {
                    synchronized (_waitOnWriteCompletion) {
                        for ( DeviceButtonConfig i : _configuredButtons ) {
                            if(_deviceButtonConfigRepo.getDao().doesExist(i.getDeviceName(),i.getButtonId()))
                                _deviceButtonConfigRepo.getDao().update(i);
                            else
                                _deviceButtonConfigRepo.getDao().insert(i);
                        }
                        _configuredButtons.clear();
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

    @Override
    protected void onResume() {
        renameOkOrConfig("OK");
        _deviceButtonConfigRepo.getAllRawData(ApplicationWideSingleton.getSelectedDeviceName());
        super.onResume();
    }

    @Override
    public void handleButtonClicks(int btnId) {

        _addedButtons.add(
                new DeviceButtonConfig(
                        btnId,
                        null,
                        ApplicationWideSingleton.getSelectedDeviceName(),
                        false,
                        _btnNames[btnId]
                )
        );
        synchronized (_waitOnWriteCompletion) {
            _hasCompletedSave = false;
        }
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
            }
        }
        dialog.dismiss();
        startActivity(transmitIntent);
    }

}
