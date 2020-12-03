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

public class RawDataRepository {

    private final RawDao _rawDataAccess;

    private final DeviceInfoRepository _deviceDataRepository;

    RawDataRepository(Application application) {
        UniversalRemoteDatabase db = UniversalRemoteDatabase.getDatabase(application);
        _rawDataAccess = db.rawDataAccess();
        _deviceDataRepository = new DeviceInfoRepository(application);
    }

    boolean insert(RawData data) {
        if(!_deviceDataRepository.doesExist(data.getDeviceName()))
            return false;

        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _rawDataAccess.insert(data)
        );
        return true;
    }

    void delete(RawData data) {
        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _rawDataAccess.delete(data)
        );
    }

    void update(RawData data) {
        UniversalRemoteDatabase.databaseWriteExecutor.execute(
                () -> _rawDataAccess.update(data)
        );
    }

    List<RawData> getAllRawData() {
        return _rawDataAccess.getAllRawData();
    }

    List<RawData> getAllRawData(String device) {
        return _rawDataAccess.getAllRawData(device);
    }

    String getIrTimingData(String device, int button) {
        return  _rawDataAccess.getIrTimingData(device, button);
    }

}
