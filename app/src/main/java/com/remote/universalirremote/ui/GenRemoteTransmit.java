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
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NavUtils;

import com.remote.universalirremote.ApplicationWideSingleton;
import com.remote.universalirremote.GenRemote;
import com.remote.universalirremote.database.DeviceButtonConfig;
import com.remote.universalirremote.network.HttpClient;
import com.remote.universalirremote.network.RawSend;

import java.net.HttpURLConnection;

public class GenRemoteTransmit extends GenRemote {

    private RawSend _sendRawIrTiming;
    private HandlerThread _sendResponseThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        _sendResponseThread = new HandlerThread("RawTvRemoteSendResponse");
        _sendResponseThread.start();
        Handler _sendResponse = new Handler(_sendResponseThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.getData().getInt(RawSend.CODE_KEY) != HttpURLConnection.HTTP_OK) {
                    Toast.makeText(getApplicationContext(),
                            "button send fail "
                                    + ((HttpClient.Request.Property) msg.getData()
                                    .getParcelable(RawSend.POST_META_KEY)).getValue(),
                            Toast.LENGTH_LONG).show();
                }
            }
        };
        _sendRawIrTiming = new RawSend(ApplicationWideSingleton.getSelectedService(),
                _sendResponse);
        super.onStart();
    }

    @Override
    protected void onResume() {
        renameOkOrConfig("Config");
        _deviceButtonConfigRepo.getAllRawData(ApplicationWideSingleton.getSelectedDeviceName());
        super.onResume();
    }

    @Override
    public void handleButtonClicks(int btnId) {
        DeviceButtonConfig selectedButton = lookupButton(btnId);
        if(selectedButton == null) {
            Toast.makeText(getApplicationContext(),
                    "not configured button", Toast.LENGTH_LONG).show();
            return;
        }
        _sendRawIrTiming.sendData(selectedButton.getIrTimingData(), selectedButton.getDeviceName());
    }

    @Override
    public void startTransitOrConfigActivity(Intent configIntent, Intent transmitIntent) {
        startActivity(configIntent);
    }

}
