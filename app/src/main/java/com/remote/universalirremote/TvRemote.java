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

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.view.View;

import com.remote.universalirremote.database.DeviceButtonConfig;
import com.remote.universalirremote.database.DeviceButtonConfigCallback;
import com.remote.universalirremote.database.DeviceButtonConfigRepository;
import com.remote.universalirremote.database.DeviceDao;
import com.remote.universalirremote.database.DeviceData;
import com.remote.universalirremote.database.DeviceInfoRepository;
import com.remote.universalirremote.database.UniversalRemoteDatabase;

import java.util.List;
import java.util.Map;

public abstract class TvRemote extends AppCompatActivity {

    protected DeviceData _selectedDevice;
    protected DeviceInfoRepository _deviceInfoRepo;
    protected DeviceButtonConfigRepository _deviceButtonConfigRepo;
    protected List<DeviceButtonConfig> _buttonConfigList;

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
                });
        _deviceInfoRepo = new DeviceInfoRepository(getApplication(), null);
        if(savedInstanceState != null) {
            String device = savedInstanceState.getString(Constant.INT_SELECTED_DEVICE);
            NsdServiceInfo service = savedInstanceState.getParcelable(Constant.INT_SERVICE_KEY);

            ApplicationWideSingleton.refreshSelectedService(service);
            _deviceButtonConfigRepo.useDatabaseExecutor(
                    () -> {
                        _selectedDevice = _deviceInfoRepo.getDao().getDevice(device);
                        _deviceButtonConfigRepo.getAllRawData(device);
                    }
            );
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        if(ApplicationWideSingleton.isSelectedDeviceValid())
            outState.putString(Constant.INT_SELECTED_DEVICE, ApplicationWideSingleton.getSelectedDevice().getDeviceName());
        if(ApplicationWideSingleton.isSelectedServiceValid())
            outState.putParcelable(Constant.INT_SERVICE_KEY, ApplicationWideSingleton.getSelectedService());
        super.onSaveInstanceState(outState);
    }


    public static int BTN_TV_PWR = 0;
    public static int BTN_TV_BACK = 1;
    public static int BTN_TV_UP = 2;
    public static int BTN_TV_DOWN = 3;
    public static int BTN_TV_LEFT = 4;
    public static int BTN_TV_RIGHT = 5;
    public static int BTN_TV_OK = 6;
    public static int BTN_TV_VOL_UP = 7;
    public static int BTN_TV_VOL_DOWN = 8;
    public static int BTN_TV_CHN_UP = 9;
    public static int BTN_TV_CHN_DOWN = 10;
    public static int BTN_TV_UNMUTE = 11;
    public static int BTN_TV_MUTE = 12;
    public static int BTN_TV_NUM_0 = 13;
    public static int BTN_TV_NUM_1 = 14;
    public static int BTN_TV_NUM_2 = 15;
    public static int BTN_TV_NUM_3 = 16;
    public static int BTN_TV_NUM_4 = 17;
    public static int BTN_TV_NUM_5 = 18;
    public static int BTN_TV_NUM_6 = 19;
    public static int BTN_TV_NUM_7 = 20;
    public static int BTN_TV_NUM_8 = 21;
    public static int BTN_TV_NUM_9 = 22;
    public static int BTN_TV_NUM_ADD = 23;
    public static int BTN_TV_HOME = 24;
    public static int BTN_TV_FAST_FORWARD = 25;
    public static int BTN_TV_FAST_BACKWARD = 26;
    public static int BTN_TV_PAUSE = 27;



    public abstract void handleButtonClicks(int btnId);

    public abstract void clickConfigureOrOK(View view);

}