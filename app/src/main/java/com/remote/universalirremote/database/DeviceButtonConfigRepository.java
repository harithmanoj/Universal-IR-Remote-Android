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


package com.remote.universalirremote.database;

import android.app.Application;

public class DeviceButtonConfigRepository {

    private final DeviceButtonConfigDao _deviceButtonConfigAccess;

    private DeviceButtonConfigCallback _getterCallback;

    public void useDatabaseExecutor(Runnable fn) {
        UniversalRemoteDatabase.databaseWriteExecutor.execute(fn);
    }

    public DeviceButtonConfigDao getDao() {
        return _deviceButtonConfigAccess;
    }

    public DeviceButtonConfigRepository(Application application, DeviceButtonConfigCallback callback) {
        UniversalRemoteDatabase db = UniversalRemoteDatabase.getDatabase(application);
        _deviceButtonConfigAccess = db.deviceButtonConfigAccess();
        _getterCallback = callback;
    }

    public void setDeviceButtonCallBack(DeviceButtonConfigCallback getter) {
        _getterCallback = getter;
    }

    public void insert(DeviceButtonConfig data) {
        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _deviceButtonConfigAccess.insert(data)
        );
    }

    public void delete(DeviceButtonConfig data) {
        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _deviceButtonConfigAccess.delete(data)
        );
    }

    public void update(DeviceButtonConfig data) {
        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _deviceButtonConfigAccess.update(data)
        );
    }

    public boolean getAllRawData() {
        if(_getterCallback == null)
            return false;

        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _getterCallback.allRawDataCallback(
                        _deviceButtonConfigAccess.getAllRawData())
        );

        return true;
    }

    public boolean getAllRawData(String device) {

        if(_getterCallback == null)
            return false;

        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _getterCallback.allRawDataForDeviceCallback(
                        _deviceButtonConfigAccess.getAllRawData(device))
        );

        return true;
    }

    public boolean getIrTimingData(String device, int button) {
        if(_getterCallback == null)
            return false;

        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _getterCallback.irTimingDataCallback(
                        _deviceButtonConfigAccess.getIrTimingData(device, button))
        );

        return true;
    }

    public boolean getDeviceButtonName(String device, int button) {
        if(_getterCallback == null)
            return false;

        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _getterCallback.deviceButtonNameCallback(
                        _deviceButtonConfigAccess.getButtonName(device, button))
        );

        return true;
    }

}
