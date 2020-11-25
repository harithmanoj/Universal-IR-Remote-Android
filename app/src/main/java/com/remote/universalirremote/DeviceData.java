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

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DeviceData")
public class DeviceData {

    @ColumnInfo(name = "deviceName")
    private String _deviceName;

    @ColumnInfo(name = "protocolInfo")
    private int _protocolInfo;

    @ColumnInfo(name = "deviceLayout")
    private int _deviceLayout;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "DeviceDataId")
    private int _id;

    public String getName() {
        return _deviceName;
    }

    public int getProtocolUsed() {
        return _protocolInfo;
    }

    public int getDeviceLayout() {
        return _deviceLayout;
    }

    public int getDeviceId() {
        return _id;
    }

}
