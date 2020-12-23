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
import android.view.View;
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
    private int _fanSpeed = Constant.AcFan.kLow;
    private int _fanSpeedPrevious = Constant.AcFan.kLow;
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

    public static final String BTN_TEMP_INC = "TemperatureIncrease",
            BTN_CLEAN = "SelfCleanMode",
            BTN_VSWING = "VerticalSwing",
            BTN_HSWING = "HorizontalSwing",
            BTN_TEMP_DEC = "TemperatureDecrease",
            BTN_TURBO = "Turbo",
            BTN_ECO = "Economy",
            BTN_REC_BEEP = "BeepOnRecieve",
            BTN_MODE = "Mode",
            BTN_POWER = "Power",
            BTN_DISPLAY_LIGHT = "DisplayLight",
            BTN_SPEED_HIGH = "HighFanSpeed",
            BTN_SPEED_LOW = "LowFanSpeed",
            BTN_SPEED_MID = "MediumFanSpeed";



    public static final String TAG = "AcRemote";

    private void updateTurbo() {
        int turboColor = R.color.transparent;
        if(_isTurboing)
            turboColor = R.color.translucentGreen;
        ((Button)findViewById(R.id.btn_turbo)).setBackgroundColor(turboColor);
    }

    private void updateSwingVertical() {
        String swingVertical = null;
        switch (_swingVertical) {
            case Constant.AcSwingv.kAuto: {
                swingVertical = "Auto Vertical Swing";
                break;
            } case Constant.AcSwingv.kHigh: {
                swingVertical = "High Vertical Swing";
                break;
            } case Constant.AcSwingv.kHighest: {
                swingVertical = "Highest Vertical Swing";
                break;
            } case Constant.AcSwingv.kLow: {
                swingVertical = "Low Vertical Swing";
                break;
            } case Constant.AcSwingv.kLowest: {
                swingVertical = "Lowest Vertical Swing";
                break;
            } case Constant.AcSwingv.kMiddle: {
                swingVertical = "Middle Vertical Swing";
                break;
            } case Constant.AcSwingv.kOff: {
                swingVertical = "Vertical Swing Off";
                break;
            }
        }
        ((TextView)findViewById()).setText(swingVertical);
    }

    private void updateSwingHorizontal() {
        String swingHorizontal = null;
        switch (_swingHorizontal) {
            case Constant.AcSwingh.kAuto: {
                swingHorizontal = "Auto Horizontal Swing";
                break;
            } case Constant.AcSwingh.kLeft: {
                swingHorizontal = "left Horizontal Swing";
                break;
            } case Constant.AcSwingh.kLeftMax: {
                swingHorizontal = "Left Max Horizontal Swing";
                break;
            } case Constant.AcSwingh.kRight: {
                swingHorizontal = "Right Horizontal Swing";
                break;
            } case Constant.AcSwingh.kRightMax: {
                swingHorizontal = "Right Max Horizontal Swing";
                break;
            } case Constant.AcSwingh.kMiddle: {
                swingHorizontal = "Middle Horizontal Swing";
                break;
            } case Constant.AcSwingh.kOff: {
                swingHorizontal = "Horizontal Swing Off";
                break;
            } case Constant.AcSwingh.kWide: {
                swingHorizontal = "Wide Horizontal Swing";
                break;
            }
        }
        ((TextView)findViewById()).setText(swingHorizontal);
    }

    public void clickSwingVertical(View view) {
        if(_swingVertical == 5) {
            _swingVertical = 0;
        } else {
            ++_swingVertical;
        }
        sendDataNow(BTN_VSWING);
        updateSwingVertical();
    }

    public void clickSwingHorizontal(View view) {
        if(_swingHorizontal == 6) {
            _swingHorizontal = 0;
        } else {
            ++_swingHorizontal;
        }
        sendDataNow(BTN_HSWING);
        updateSwingHorizontal();
    }

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

    public void clickTemperatureIncrement(View view) {
        ++_temperatureStatus;
        sendDataNow(BTN_TEMP_INC);
        updateTemperature();
    }

    public void clickTemperatureDecrement(View view) {
        --_temperatureStatus;
        sendDataNow(BTN_TEMP_DEC);
        updateTemperature();
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

    public void clickMode(View view) {
        if(_modeStatus == 4)
            _modeStatus = 0;
        else
            ++_modeStatus;
        sendDataNow(BTN_MODE);
        updateModeStatus();
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

    public void clickPower(View view) {
        _powerStatus = !_powerStatus;
        sendDataNow(BTN_POWER);
        updatePowerStatus();
    }

    private void updateFanSpeed() {
        int highBack = R.color.transparent;
        int lowBack = R.color.transparent;
        int medBack = R.color.transparent;
        switch(_fanSpeed) {
            case Constant.AcFan.kHigh: {
                highBack = R.color.translucentGreen;
                break;
            } case Constant.AcFan.kLow: {
                lowBack = R.color.translucentGreen;
                break;
            } case Constant.AcFan.kMedium: {
                medBack = R.color.translucentGreen;
                break;
            }
        }
        ((Button)findViewById(R.id.btn_speedHigh)).setBackgroundColor(
                getResources().getColor(highBack));
        ((Button)findViewById(R.id.btn_speedLow)).setBackgroundColor(
                getResources().getColor(lowBack));
        ((Button)findViewById(R.id.btn_speedMedium)).setBackgroundColor(
                getResources().getColor(medBack));
    }

    public void clickFanHigh(View view) {
        if(_fanSpeed == Constant.AcFan.kHigh) {
            Toast.makeText(getApplicationContext(),
                    "Fan speed is already high", Toast.LENGTH_SHORT).show();
            return;
        }
        _fanSpeedPrevious = _fanSpeed;
        _fanSpeed = Constant.AcFan.kHigh;
        sendDataNow(BTN_SPEED_HIGH);
        updateFanSpeed();
    }

    public void clickFanLow(View view) {
        if(_fanSpeed == Constant.AcFan.kLow) {
            Toast.makeText(getApplicationContext(),
                    "Fan speed is already low", Toast.LENGTH_SHORT).show();
            return;
        }
        _fanSpeedPrevious = _fanSpeed;
        _fanSpeed = Constant.AcFan.kLow;
        sendDataNow(BTN_SPEED_LOW);
        updateFanSpeed();
    }

    public void clickFanMedium(View view) {
        if(_fanSpeed == Constant.AcFan.kMedium) {
            Toast.makeText(getApplicationContext(),
                    "Fan speed is already medium", Toast.LENGTH_SHORT).show();
            return;
        }
        _fanSpeedPrevious = _fanSpeed;
        _fanSpeed = Constant.AcFan.kMedium;
        sendDataNow(BTN_SPEED_MID);
        updateFanSpeed();
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
                    runOnUiThread( () -> {
                        undoButtonClick(((HttpClient.Request.Property) msg.getData()
                                .getParcelable(ACSend.POST_META_KEY)).getValue());
                        Toast.makeText(getApplicationContext(),
                                "undoing.. : button send fail "
                                        + ((HttpClient.Request.Property) msg.getData()
                                        .getParcelable(ACSend.POST_META_KEY)).getValue(),
                                Toast.LENGTH_SHORT).show();
                    });
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

    private void sendDataNow(String btnName) {
        _sendAcStatusUpdate.sendData(
                ApplicationWideSingleton.getSelectedDevice().getProtocolInfo(),
                0,
                _powerStatus,
                _modeStatus,
                _temperatureStatus,
                _isTemperatureInCelsius,
                _fanSpeed,
                _swingVertical,
                _swingHorizontal,
                _isQuiet,
                _isTurboing,
                _isEconomy,
                _isDisplayOn,
                _isFilterModeOn,
                _isSelfCleanModeOn,
                _isReceivingBeepOn,
                _sleepMinutes,
                _clockMinutesSinceMidnight,
                btnName
        );
    }

    private void undoButtonClick(String btnName) {

    }

}