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
//


package com.remote.universalirremote.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DeviceButtonConfigDao {
    @Insert
    void insert(DeviceButtonConfig data);

    @Delete
    void delete(DeviceButtonConfig data);

    @Update
    void update(DeviceButtonConfig data);

    @Query("SELECT * FROM DeviceButtonConfig WHERE deviceName = :device")
    List<DeviceButtonConfig> getAllRawData(String device);

    @Query("SELECT * FROM DeviceButtonConfig")
    List<DeviceButtonConfig> getAllRawData();

    @Query("SELECT timingData FROM DeviceButtonConfig WHERE ((deviceName = :device) AND (deviceButtonId = :button))")
    String getIrTimingData(String device, int button);

    @Query("SELECT deviceButtonName FROM DeviceButtonConfig WHERE ((deviceName = :device) AND (deviceButtonId = :button))")
    String getButtonName(String device, int button);

    @Query("SELECT EXISTS(SELECT * FROM DEVICEBUTTONCONFIG WHERE ((deviceButtonId = :button) AND (deviceName = :device)))")
    boolean doesExist(String device, int button);

}
