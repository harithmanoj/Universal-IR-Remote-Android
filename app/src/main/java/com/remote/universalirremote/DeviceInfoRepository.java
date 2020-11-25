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

public class DeviceInfoRepository {
    private final DeviceDao _deviceDataAccess;

    DeviceInfoRepository(Application application) {
        UniversalRemoteDatabase db = UniversalRemoteDatabase.getDatabase(application);
        _deviceDataAccess = db.deviceDataAccess();
    }

    List<DeviceData> getAllDevices() {
        return _deviceDataAccess.getAllDevices();
    }

    void insert(DeviceData device) {
        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _deviceDataAccess.insert(device)
        );
    }

    void delete(DeviceData device) {
        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _deviceDataAccess.delete(device)
        );
    }

    void update(DeviceData device) {
        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _deviceDataAccess.update(device)
        );
    }
}
