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

import java.util.List;

//
//   Class for clean asynchronous access to DeviceData table
//   all query are run as asynchronously using executors.
//
public class DeviceInfoRepository {
    private final DeviceDao _deviceDataAccess;

    private DeviceDataCallback _getterCallback;

    public DeviceInfoRepository(Application application, DeviceDataCallback getter) {
        UniversalRemoteDatabase db = UniversalRemoteDatabase.getDatabase(application);
        _deviceDataAccess = db.deviceDataAccess();
        _getterCallback = getter;
    }

    public List<DeviceData> getAllDevices() {
        return _deviceDataAccess.getAllDevices();
    }

    public void insert(DeviceData device) {
        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _deviceDataAccess.insert(device)
        );
    }

    public void delete(DeviceData device) {
        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _deviceDataAccess.delete(device)
        );
    }

    public void update(DeviceData device) {
        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _deviceDataAccess.update(device)
        );
    }

    public boolean doesExist(String name) {
        return (_deviceDataAccess.getDevice(name) != null);
    }


    public boolean getNames() {
        if(_getterCallback == null)
            return false;

        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                ()->_getterCallback.namesCallback(_deviceDataAccess.getNames())
        );
        return true;
    }

    public boolean getDevice(String name) {
        if(_getterCallback == null)
            return false;

        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                ()->_getterCallback.deviceWithNameCallback(_deviceDataAccess.getDevice(name))
        );
        return true;
    }

    public boolean getLayout(String name) {

        if(_getterCallback == null)
            return false;

        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                ()->_getterCallback.layoutCallback(_deviceDataAccess.getLayout(name))
        );
        return true;
    }

    public boolean getProtocolUsed(String name) {
        if(_getterCallback == null)
            return false;

        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                ()->_getterCallback.protocolCallback(_deviceDataAccess.getProtocolUsed(name))
        );
        return true;
    }
}
