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

package com.remote.universalirremote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.remote.universalirremote.database.DeviceButtonConfig;
import com.remote.universalirremote.database.DeviceButtonConfigCallback;
import com.remote.universalirremote.database.DeviceButtonConfigRepository;
import com.remote.universalirremote.database.DeviceDataParcelable;
import com.remote.universalirremote.database.DeviceInfoRepository;
import com.remote.universalirremote.ui.TvRemoteConfigure;
import com.remote.universalirremote.ui.TvRemoteTransmit;

import java.util.List;

public abstract class TvRemote extends AppCompatActivity {

    protected DeviceInfoRepository _deviceInfoRepo;
    protected DeviceButtonConfigRepository _deviceButtonConfigRepo;
    protected List<DeviceButtonConfig> _buttonConfigList;

    public static final String TAG = "TvRemote";

    protected void renameOkOrConfig(String name) {
        ((Button)findViewById(R.id.btn_okConfig)).setText(name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_remote);
        _deviceButtonConfigRepo = new DeviceButtonConfigRepository(getApplication(),
                new DeviceButtonConfigCallback() {
                    @Override
                    public void allRawDataCallback(List<DeviceButtonConfig> allRawData) {

                    }

                    @Override
                    public void allRawDataForDeviceCallback(List<DeviceButtonConfig> allDeviceRawData) {
                        _buttonConfigList = allDeviceRawData;
                    }

                    @Override
                    public void irTimingDataCallback(String irTiming) {

                    }

                    @Override
                    public void deviceButtonNameCallback(String buttonName) {

                    }

                    @Override
                    public void doesEntryExist(boolean doesExist) {

                    }

                    @Override
                    public void buttonConfig(DeviceButtonConfig buttonConfigData) {

                    }
                });
        _deviceInfoRepo = new DeviceInfoRepository(getApplication(), null);

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

                _deviceButtonConfigRepo.getAllRawData(device.getDeviceName());
            }
        }

        if(savedInstanceState != null) {
            DeviceDataParcelable device = savedInstanceState.getParcelable(Constant.INT_SELECTED_DEVICE);
            NsdServiceInfo service = savedInstanceState.getParcelable(Constant.INT_SERVICE_KEY);
            ApplicationWideSingleton.refreshSelectedDevice(device);
            ApplicationWideSingleton.refreshSelectedService(service);
            _deviceButtonConfigRepo.getAllRawData(device.getDeviceName());
        }
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
                _deviceButtonConfigRepo.getAllRawData(device.getDeviceName());
            }
        }
        super.onStart();
    }

    protected DeviceButtonConfig lookupButton(int btnId) {

        for(DeviceButtonConfig i : _buttonConfigList) {
            if(i.getButtonId() == btnId)
                return i;
        }

        return null;
    }

    public static final String[] _btnNames =  {
            "BTN_TV_PWR",
            "BTN_TV_BACK",
            "BTN_TV_UP",
            "BTN_TV_DOWN",
            "BTN_TV_LEFT",
            "BTN_TV_RIGHT",
            "BTN_TV_OK",
            "BTN_TV_VOL_UP",
            "BTN_TV_VOL_DOWN",
            "BTN_TV_CHN_UP",
            "BTN_TV_CHN_DOWN",
            "BTN_TV_UNMUTE",
            "BTN_TV_MUTE",
            "BTN_TV_NUM_0",
            "BTN_TV_NUM_1",
            "BTN_TV_NUM_2",
            "BTN_TV_NUM_3",
            "BTN_TV_NUM_4",
            "BTN_TV_NUM_5",
            "BTN_TV_NUM_6",
            "BTN_TV_NUM_7",
            "BTN_TV_NUM_8",
            "BTN_TV_NUM_9",
            "BTN_TV_NUM_ADD",
            "BTN_TV_HOME",
            "BTN_TV_FAST_FORWARD",
            "BTN_TV_FAST_BACKWARD",
            "BTN_TV_PAUSE"
    };

    public static final int BTN_TV_PWR = 0;
    public static final int BTN_TV_BACK = 1;
    public static final int BTN_TV_UP = 2;
    public static final int BTN_TV_DOWN = 3;
    public static final int BTN_TV_LEFT = 4;
    public static final int BTN_TV_RIGHT = 5;
    public static final int BTN_TV_OK = 6;
    public static final int BTN_TV_VOL_UP = 7;
    public static final int BTN_TV_VOL_DOWN = 8;
    public static final int BTN_TV_CHN_UP = 9;
    public static final int BTN_TV_CHN_DOWN = 10;
    public static final int BTN_TV_UNMUTE = 11;
    public static final int BTN_TV_MUTE = 12;
    public static final int BTN_TV_NUM_0 = 13;
    public static final int BTN_TV_NUM_1 = 14;
    public static final int BTN_TV_NUM_2 = 15;
    public static final int BTN_TV_NUM_3 = 16;
    public static final int BTN_TV_NUM_4 = 17;
    public static final int BTN_TV_NUM_5 = 18;
    public static final int BTN_TV_NUM_6 = 19;
    public static final int BTN_TV_NUM_7 = 20;
    public static final int BTN_TV_NUM_8 = 21;
    public static final int BTN_TV_NUM_9 = 22;
    public static final int BTN_TV_NUM_ADD = 23;
    public static final int BTN_TV_HOME = 24;
    public static final int BTN_TV_FAST_FORWARD = 25;
    public static final int BTN_TV_FAST_BACKWARD = 26;
    public static final int BTN_TV_PAUSE = 27;

    public abstract void handleButtonClicks(int btnId);

    public abstract void startTransitOrConfigActivity(Intent configIntent, Intent transmitIntent);

    public void clickConfigureOrOK(View view) {
        Intent config = new Intent(this, TvRemoteConfigure.class);
        Intent transmit = new Intent(this, TvRemoteTransmit.class);

        config.putExtra(Constant.INT_SERVICE_KEY, ApplicationWideSingleton.getSelectedService());
        config.putExtra(Constant.INT_SELECTED_DEVICE,
                new DeviceDataParcelable(ApplicationWideSingleton.getSelectedDevice()));

        transmit.putExtra(Constant.INT_SERVICE_KEY, ApplicationWideSingleton.getSelectedService());
        transmit.putExtra(Constant.INT_SELECTED_DEVICE,
                new DeviceDataParcelable(ApplicationWideSingleton.getSelectedDevice()));

        startTransitOrConfigActivity(config, transmit);
    }

    public void clickButton(View view) {
        int id;
        if(view.getId() == R.id.btn_Power) {
            id = (BTN_TV_PWR);
        } else if ( view.getId() == R.id.btn_Back) {
            id = (BTN_TV_BACK);
        } else if (view.getId() == R.id.btn_ArrowUp) {
            id = (BTN_TV_UP);
        } else if (view.getId() == R.id.btn_ArrowDown) {
            id = (BTN_TV_DOWN);
        } else if (view.getId() == R.id.btn_ArrowLeft) {
            id = (BTN_TV_LEFT);
        } else if (view.getId() == R.id.btn_ArrowRight) {
            id = (BTN_TV_RIGHT);
        } else if (view.getId() == R.id.btn_Ok) {
            id = (BTN_TV_OK);
        } else if (view.getId() == R.id.btn_VolumeUp) {
            id = (BTN_TV_VOL_UP);
        } else if (view.getId() == R.id.btn_VolumeDown) {
            id = (BTN_TV_VOL_DOWN);
        } else if (view.getId() == R.id.btn_ChannelUp) {
            id = BTN_TV_CHN_UP;
        } else if (view.getId() == R.id.btn_ChannelDown) {
            id = BTN_TV_CHN_DOWN;
        } else if (view.getId() == R.id.btn_Mute) {
            id = BTN_TV_MUTE;
        } else if (view.getId() == R.id.btn_UnMute) {
            id = BTN_TV_UNMUTE;
        } else if (view.getId() == R.id.btn_Num0) {
            id = BTN_TV_NUM_0;
        } else if (view.getId() == R.id.btn_Num1) {
            id = BTN_TV_NUM_1;
        } else if (view.getId() == R.id.btn_Num2) {
            id = BTN_TV_NUM_2;
        } else if (view.getId() == R.id.btn_Num3) {
            id = BTN_TV_NUM_3;
        } else if (view.getId() == R.id.btn_Num4) {
            id = BTN_TV_NUM_4;
        } else if (view.getId() == R.id.btn_Num5) {
            id = BTN_TV_NUM_5;
        } else if (view.getId() == R.id.btn_Num6) {
            id = BTN_TV_NUM_6;
        } else if (view.getId() == R.id.btn_Num7) {
            id = BTN_TV_NUM_7;
        } else if (view.getId() == R.id.btn_Num8) {
            id = BTN_TV_NUM_8;
        } else if (view.getId() == R.id.btn_Num9) {
            id = BTN_TV_NUM_9;
        } else if (view.getId() == R.id.btn_AddNum) {
            id = BTN_TV_NUM_ADD;
        } else if (view.getId() == R.id.btn_home) {
            id = BTN_TV_HOME;
        } else if (view.getId() == R.id.btn_fastForward) {
            id = BTN_TV_FAST_FORWARD;
        } else if (view.getId() == R.id.btn_fastBack) {
            id = BTN_TV_FAST_BACKWARD;
        } else if (view.getId() == R.id.btn_pause) {
            id = BTN_TV_PAUSE;
        } else {
            return;
        }

        handleButtonClicks(id);
    }
}