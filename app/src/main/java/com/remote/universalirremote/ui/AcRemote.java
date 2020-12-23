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

import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.remote.universalirremote.ApplicationWideSingleton;
import com.remote.universalirremote.Constant;
import com.remote.universalirremote.R;
import com.remote.universalirremote.database.DeviceDataParcelable;
import com.remote.universalirremote.network.ACSend;
import com.remote.universalirremote.network.HttpClient;
import com.remote.universalirremote.network.RawSend;

import java.net.HttpURLConnection;

public class AcRemote extends AppCompatActivity {

    private HandlerThread _sendResponseHandlerThread;
    private ACSend _sendAcStatusUpdate;

    public static final String TAG = "AcRemote";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ac_remote);

        Intent intent = getIntent();
        if(intent != null) {
            Log.d(TAG, "intent called now saving");
            DeviceDataParcelable device = intent.getParcelableExtra(Constant.INT_SELECTED_DEVICE);
            NsdServiceInfo service = intent.getParcelableExtra(Constant.INT_SERVICE_KEY);

            if(service != null )
                ApplicationWideSingleton.refreshSelectedService(service);
            if(device != null) {
                ApplicationWideSingleton.refreshSelectedDevice(device);
                Log.d(TAG, "refreshing device");
            }
        } else if(savedInstanceState != null) {
            DeviceDataParcelable device = savedInstanceState.getParcelable(Constant.INT_SELECTED_DEVICE);
            NsdServiceInfo service = savedInstanceState.getParcelable(Constant.INT_SERVICE_KEY);
            ApplicationWideSingleton.refreshSelectedDevice(device);
            ApplicationWideSingleton.refreshSelectedService(service);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        if(ApplicationWideSingleton.isSelectedDeviceValid())
            outState.putParcelable(Constant.INT_SELECTED_DEVICE,
                    new DeviceDataParcelable(ApplicationWideSingleton.getSelectedDevice()));
        if(ApplicationWideSingleton.isSelectedServiceValid())
            outState.putParcelable(Constant.INT_SERVICE_KEY, ApplicationWideSingleton.getSelectedService());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {

        Intent intent = getIntent();
        if(intent != null) {
            Log.d(TAG, "intent called now saving");

            DeviceDataParcelable device = intent.getParcelableExtra(Constant.INT_SELECTED_DEVICE);
            NsdServiceInfo service = intent.getParcelableExtra(Constant.INT_SERVICE_KEY);

            if(service != null )
                ApplicationWideSingleton.refreshSelectedService(service);
            if(device != null) {
                ApplicationWideSingleton.refreshSelectedDevice(device);
                Log.d(TAG, "refreshing device");
            }
        }

        _sendResponseHandlerThread = new HandlerThread("AcRemoteSendResponse");
        _sendResponseHandlerThread.start();
        Handler sendResponse = new Handler(_sendResponseHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.getData().getInt(RawSend.CODE_KEY) != HttpURLConnection.HTTP_OK) {
                    Toast.makeText(getApplicationContext(),
                            "button send fail "
                                    + ((HttpClient.Request.Property) msg.getData()
                                    .getParcelable(RawSend.POST_META_KEY)).getValue(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        _sendAcStatusUpdate = new ACSend(ApplicationWideSingleton.getSelectedService(),
                sendResponse,
                errorString -> runOnUiThread(
                        () -> Toast.makeText(getApplicationContext(),
                                "Network error: " + errorString, Toast.LENGTH_SHORT).show()
                ));
        super.onStart();
    }

}