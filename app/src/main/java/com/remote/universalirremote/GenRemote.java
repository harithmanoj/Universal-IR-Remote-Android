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
import android.widget.TextView;

import com.remote.universalirremote.database.DeviceButtonConfig;
import com.remote.universalirremote.database.DeviceButtonConfigCallback;
import com.remote.universalirremote.database.DeviceButtonConfigRepository;
import com.remote.universalirremote.database.DeviceData;
import com.remote.universalirremote.database.DeviceDataParcelable;
import com.remote.universalirremote.database.DeviceInfoRepository;
import com.remote.universalirremote.ui.TvRemoteConfigure;
import com.remote.universalirremote.ui.TvRemoteTransmit;

import java.util.List;

public abstract class GenRemote extends AppCompatActivity {

    protected DeviceInfoRepository _deviceInfoRepo;
    protected DeviceButtonConfigRepository _deviceButtonConfigRepo;
    protected List<DeviceButtonConfig> _buttonConfigList;

    public static final String TAG = "GenRemote";

    public static final int BTN_GEN_POWER = 0,
            BTN_GEN_2 = 1,
            BTN_GEN_3 = 2,
            BTN_GEN_4 = 3,
            BTN_GEN_5 = 4,
            BTN_GEN_6 = 5,
            BTN_GEN_7 = 6,
            BTN_GEN_8 = 7,
            BTN_GEN_9 = 8,
            BTN_GEN_10 = 9,
            BTN_GEN_11 = 10,
            BTN_GEN_12 = 11,
            BTN_GEN_13 = 12,
            BTN_GEN_OK = 13,
            BTN_GEN_LEFT = 14,
            BTN_GEN_RIGHT = 15,
            BTN_GEN_UP = 16,
            BTN_GEN_DOWN = 17,
            BTN_GEN_A_UP = 18,
            BTN_GEN_A_DOWN = 19,
            BTN_GEN_X_UP = 20,
            BTN_GEN_X_DOWN = 21;


    protected void renameOkOrConfig(String name) {
        ((TextView)findViewById(R.id.btn_OKorConfig)).setText(name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gen_remote);
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

                _deviceButtonConfigRepo.useDatabaseExecutor(
                        () -> _deviceButtonConfigRepo.getAllRawData(device.getDeviceName())
                );
            }
        }

        if(savedInstanceState != null) {
            DeviceDataParcelable device = savedInstanceState.getParcelable(Constant.INT_SELECTED_DEVICE);
            NsdServiceInfo service = savedInstanceState.getParcelable(Constant.INT_SERVICE_KEY);
            ApplicationWideSingleton.refreshSelectedDevice(device);
            ApplicationWideSingleton.refreshSelectedService(service);
            _deviceButtonConfigRepo.useDatabaseExecutor(
                    () -> _deviceButtonConfigRepo.getAllRawData(device.getDeviceName())
            );
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

    protected DeviceButtonConfig lookupButton(int btnId) {

        for(DeviceButtonConfig i : _buttonConfigList) {
            if(i.getButtonId() == btnId)
                return i;
        }

        return null;
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
                _deviceButtonConfigRepo.useDatabaseExecutor(
                        () -> _deviceButtonConfigRepo.getAllRawData(device.getDeviceName())
                );
            }
        }
        super.onStart();
    }

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
        int id = view.getId();
        if (id == R.id.btn_power) {
            handleButtonClicks(BTN_GEN_POWER);
        } else if (id == R.id.btn_2) {
            handleButtonClicks(BTN_GEN_2);
        } else if (id == R.id.btn_3) {
            handleButtonClicks(BTN_GEN_3);
        } else if (id == R.id.btn_4) {
            handleButtonClicks(BTN_GEN_4);
        } else if (id == R.id.btn_5) {
            handleButtonClicks(BTN_GEN_5);
        } else if (id == R.id.btn_6) {
            handleButtonClicks(BTN_GEN_6);
        } else if (id == R.id.btn_7) {
            handleButtonClicks(BTN_GEN_7);
        } else if (id == R.id.btn_8) {
            handleButtonClicks(BTN_GEN_8);
        } else if (id == R.id.btn_9) {
            handleButtonClicks(BTN_GEN_9);
        } else if (id == R.id.btn_10) {
            handleButtonClicks(BTN_GEN_10);
        } else if (id == R.id.btn_11) {
            handleButtonClicks(BTN_GEN_11);
        } else if (id == R.id.btn_12) {
            handleButtonClicks(BTN_GEN_12);
        } else if (id == R.id.btn_13) {
            handleButtonClicks(BTN_GEN_13);
        } else if (id == R.id.btn_ok) {
            handleButtonClicks(BTN_GEN_OK);
        } else if (id == R.id.btn_left) {
            handleButtonClicks(BTN_GEN_LEFT);
        } else if (id == R.id.btn_right) {
            handleButtonClicks(BTN_GEN_RIGHT);
        } else if (id == R.id.btn_up) {
            handleButtonClicks(BTN_GEN_UP);
        } else if (id == R.id.btn_down) {
            handleButtonClicks(BTN_GEN_DOWN);
        } else if (id == R.id.btn_increment_a) {
            handleButtonClicks(BTN_GEN_A_UP);
        } else if (id == R.id.btn_decrement_b) {
            handleButtonClicks(BTN_GEN_A_DOWN);
        } else if (id == R.id.btn_increment_x) {
            handleButtonClicks(BTN_GEN_X_UP);
        } else if (id == R.id.btn_decrement_x) {
            handleButtonClicks(BTN_GEN_X_DOWN);
        }
    }
}