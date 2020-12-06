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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.annotation.NonNull;

import com.remote.universalirremote.ApplicationWideSingleton;
import com.remote.universalirremote.TvRemote;
import com.remote.universalirremote.database.DeviceButtonConfig;
import com.remote.universalirremote.network.RawGet;

import java.util.ArrayList;

public class TvRemoteConfigure extends TvRemote {

    private RawGet _getRawIrTiming;
    private HandlerThread _getResponseHandlerThread;
    private ArrayList<DeviceButtonConfig> _addedButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _addedButtons = new ArrayList<>();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        _getResponseHandlerThread = new HandlerThread("RawTvRemoteGetResponse");
        Handler _getResponseHandler = new Handler(_getResponseHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                int id = msg.getData().getInt(RawGet.BUTTON_ID_KEY);
                for (int i = 0; i < _addedButtons.size(); ++i) {
                    if (_addedButtons.get(i).getButtonId() == id) {
                        DeviceButtonConfig current = _addedButtons.get(i);
                        _addedButtons.remove(i);
                        _addedButtons.add(
                                new DeviceButtonConfig(
                                        id,
                                        msg.getData().getString(RawGet.RAW_KEY),
                                        current.getDeviceName(),
                                        current.isEditableName(),
                                        current.getDeviceButtonName()
                                )
                        );
                        break;
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
    protected void onPause() {
        for (DeviceButtonConfig i : _addedButtons)
            if(i.getIrTimingData() != null)
            {
                _deviceButtonConfigRepo.insert(i);
                _addedButtons.remove(i);
            }
        super.onPause();
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
        _getRawIrTiming.getData(btnId);
    }

    @Override
    public void startTransitOrConfigActivity(Intent configIntent, Intent transmitIntent) {
        startActivity(transmitIntent);
    }

}
