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
        primaryKeys = { "deviceButtonId", "deviceName" }
)
public class DeviceButtonConfig {

    @ColumnInfo(name = "deviceButtonId")
    private final int _buttonId;

    @ColumnInfo(name = "timingData")
    private final String _irTimingData;

    @NonNull
    @ColumnInfo(name = "deviceName", index = true)
    private final String _deviceName;

    @ColumnInfo(name = "isEditableName")
    private final boolean _isEditableName;

    @NonNull
    @ColumnInfo(name = "deviceButtonName")
    private final String _deviceButtonName;

    public DeviceButtonConfig(int _buttonId, String _irTimingData,
                              @NonNull String _deviceName, boolean _isEditableName,
                              @NonNull String _deviceButtonName) {
        this._buttonId = _buttonId;
        this._irTimingData = _irTimingData;
        this._deviceName = _deviceName;
        this._isEditableName = _isEditableName;
        this._deviceButtonName = _deviceButtonName;
    }

    public int getButtonId(){
        return _buttonId;
    }

    public String getIrTimingData() {
        return _irTimingData;
    }

    public String getDeviceName() {
        return _deviceName;
    }

    public boolean isEditableName() {
        return _isEditableName;
    }

    public String getDeviceButtonName() {
        return _deviceButtonName;
    }

}
