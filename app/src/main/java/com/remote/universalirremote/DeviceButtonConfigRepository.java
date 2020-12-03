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

import android.app.Application;

import java.util.List;

public class DeviceButtonConfigRepository {

    private final DeviceButtonConfigDao _deviceButtonConfigAccess;

    private final DeviceInfoRepository _deviceDataRepository;

    DeviceButtonConfigRepository(Application application) {
        UniversalRemoteDatabase db = UniversalRemoteDatabase.getDatabase(application);
        _deviceButtonConfigAccess = db.deviceButtonConfigAccess();
        _deviceDataRepository = new DeviceInfoRepository(application);
    }

    boolean insert(DeviceButtonConfig data) {
        if(!_deviceDataRepository.doesExist(data.getDeviceName()))
            return false;

        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _deviceButtonConfigAccess.insert(data)
        );
        return true;
    }

    void delete(DeviceButtonConfig data) {
        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _deviceButtonConfigAccess.delete(data)
        );
    }

    void update(DeviceButtonConfig data) {
        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _deviceButtonConfigAccess.update(data)
        );
    }

    List<DeviceButtonConfig> getAllRawData() {
        return _deviceButtonConfigAccess.getAllRawData();
    }

    List<DeviceButtonConfig> getAllRawData(String device) {
        return _deviceButtonConfigAccess.getAllRawData(device);
    }

    String getIrTimingData(String device, int button) {
        return  _deviceButtonConfigAccess.getIrTimingData(device, button);
    }

    String getDeviceButtonName(String device, int button) {
        return  _deviceButtonConfigAccess.getButtonName(device, button);
    }

}
