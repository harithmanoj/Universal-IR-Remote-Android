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
import androidx.room.ForeignKey;

@Entity(
        tableName = "DeviceButtonConfig",
        foreignKeys = @ForeignKey(
                entity = DeviceData.class,
                parentColumns = "deviceNameId",
                childColumns = "deviceName"),
        primaryKeys = { "deviceButtonId", "_deviceName" }
)
public class DeviceButtonConfig {

    @NonNull
    @ColumnInfo(name = "deviceButtonId")
    private int _buttonId;

    @NonNull
    @ColumnInfo(name = "timingData")
    private String _irTimingData;

    @NonNull
    @ColumnInfo(name = "deviceName")
    private String _deviceName;

    @NonNull
    @ColumnInfo(name = "isEditableName")
    private boolean _isEditableName;

    @NonNull
    @ColumnInfo(name = "deviceButtonName")
    private String _deviceButtonName;

    public DeviceButtonConfig(int button, String timing, String device, boolean isEditable, String name) {
        _buttonId = button;
        _irTimingData = timing;
        _deviceName = device;
        _isEditableName = isEditable;
        _deviceButtonName = name;
    }

    public int getButtonId(){
        return _buttonId;
    }

    public String getTimingData() {
        return _irTimingData;
    }

    public String getDeviceName() {
        return _deviceName;
    }

    public boolean isButtonNameEditable() {
        return _isEditableName;
    }

    public String getDeviceButtonName() {
        return _deviceButtonName;
    }

}
