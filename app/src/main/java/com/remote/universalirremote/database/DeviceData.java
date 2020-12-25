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

package com.remote.universalirremote.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//
// Class to encapsulate device data table DeviceData in database
//
//
@Entity(tableName = "DeviceData")
public class DeviceData {

    // Name of device also serves as primary key of device
    @PrimaryKey
    @ColumnInfo(name = "deviceNameId")
    @NonNull
    private final String _deviceName;

    // Type of protocol
    @ColumnInfo(name = "protocolInfo")
    private final int _protocolInfo;

    // Type of layout
    @ColumnInfo(name = "deviceLayout")
    private final int _deviceLayout;

    @ColumnInfo(name = "modelInfo")
    private final int _deviceModel;

    // Constructor
    public DeviceData(@NonNull String _deviceName, int _protocolInfo, int _deviceLayout, int _deviceModel) {
        this._deviceLayout = _deviceLayout;
        this._deviceName = _deviceName;
        this._protocolInfo = _protocolInfo;
        this._deviceModel = _deviceModel;
    }

    // name getter
    public String getDeviceName() { return _deviceName; }

    // protocol getter
    public int getProtocolInfo() {
        return _protocolInfo;
    }

    // layout getter
    public int getDeviceLayout() { return _deviceLayout; }

    // model getter
    public int getDeviceModel() { return _deviceModel; }

}
