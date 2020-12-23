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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
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

    private boolean _powerStatus = false;
    private int _modeStatus = Constant.AcMode.kAuto;
    private int _temperatureStatus = 16;
    private boolean _isTemperatureInCelsius = true; // false is Fahrenheit
    private int _fanSpeed = Constant.AcFan.kAuto;
    private int _swingVertical = Constant.AcSwingv.kAuto;
    private int _swingHorizontal = Constant.AcSwingh.kAuto;
    private boolean _isQuiet = false;
    private boolean _isTurboing = false;
    private boolean _isEconomy = false;
    private boolean _isDisplayOn = true;
    private boolean _isFilterModeOn = true;
    private boolean _isSelfCleanModeOn = true;
    private boolean _isReceivingBeepOn = true;
    private int _sleepMinutes = 0;
    private int _clockMinutesSinceMidnight = -10;

    public static final String TAG = "AcRemote";

    private String getTemperatureUnit() {
        if(_isTemperatureInCelsius)
            return "C";
        else
            return "F";
    }

    private int updateTemperature() {
        ((TextView)findViewById(R.id.text_temperatureDisplay)).setText(
                ((Integer)_temperatureStatus).toString() + getString(R.string.degree)
                        + " " + getTemperatureUnit()
        );
        return _temperatureStatus;
    }

    private void updateModeStatus() {
        String mode = null;
        switch(_modeStatus) {
            case Constant.AcMode.kAuto: {
                mode = "Auto Mode";
                break;
            } case Constant.AcMode.kCool: {
                mode = "Cool Mode";
                break;
            } case Constant.AcMode.kDry: {
                mode = "Dry Mode";
                break;
            } case Constant.AcMode.kFan: {
                mode = "Fan Mode";
                break;
            } case Constant.AcMode.kHeat: {
                mode = "Heat Mode";
                break;
            }
        }
        ((TextView)findViewById()).setText(mode);
    }

    private void updatePowerStatus() {
        if(_powerStatus) {
            ((Button)findViewById(R.id.btn_power)).setBackgroundColor(
                    getResources().getColor(R.color.translucentGreen));
        } else {
            ((Button)findViewById(R.id.btn_power)).setBackgroundColor(
                    getResources().getColor(R.color.transparent));
        }
    }

    private void updateFanSpeed() {
        String fan = null;
        switch(_fanSpeed) {
            case Constant.AcFan.kAuto: {
                fan = "Auto Speed";
                break;
            } case Constant.AcFan.kHigh: {
                fan = "High Speed";
                break;
            } case Constant.AcFan.kLow: {
                fan = "Low Speed";
                break;
            } case Constant.AcFan.kMedium: {
                fan = "Medium Speed";
                break;
            }
        }
        ((TextView)findViewById()).setText(fan);
    }
    

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
        super.onSaveInstanceState(outState);
        if(ApplicationWideSingleton.isSelectedDeviceValid())
            outState.putParcelable(Constant.INT_SELECTED_DEVICE,
                    new DeviceDataParcelable(ApplicationWideSingleton.getSelectedDevice()));
        if(ApplicationWideSingleton.isSelectedServiceValid())
            outState.putParcelable(Constant.INT_SERVICE_KEY, ApplicationWideSingleton.getSelectedService());
    }

    @Override
    protected void onStart() {
        super.onStart();
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
                if (msg.getData().getInt(ACSend.CODE_KEY) != HttpURLConnection.HTTP_OK) {
                    Toast.makeText(getApplicationContext(),
                            "button send fail "
                                    + ((HttpClient.Request.Property) msg.getData()
                                    .getParcelable(ACSend.POST_META_KEY)).getValue(),
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

}