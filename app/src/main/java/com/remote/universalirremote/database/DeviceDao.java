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

//
//    Class to manage SQL query for table DeviceData
//
@Dao
public interface DeviceDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(DeviceData data);

    @Delete
    void delete(DeviceData data);

    @Update
    void update(DeviceData data);

    @Query("SELECT * FROM DeviceData")
    List<DeviceData> getAllDevices();

    @Query("SELECT deviceNameId FROM DeviceData")
    List<String> getNames();

    @Query("SELECT * FROM DeviceData WHERE deviceNameId = :name")
    DeviceData getDevice(String name);

    @Query("SELECT deviceLayout FROM DeviceData WHERE deviceNameId = :name")
    int getLayout(String name);

    @Query("SELECT protocolInfo FROM DeviceData WHERE deviceNameId = :name")
    int getProtocolUsed(String name);

    @Query("SELECT EXISTS(SELECT * FROM DeviceData WHERE deviceNameId = :name)")
    boolean doesDeviceExist(String name);
}
