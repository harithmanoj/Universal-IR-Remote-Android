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

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class DeviceDataParcelable extends DeviceData implements Parcelable {

    public DeviceDataParcelable(@NonNull String _deviceName, int _protocolInfo, int _deviceLayout, int _deviceModel) {
        super(_deviceName, _protocolInfo, _deviceLayout, _deviceModel);
    }

    public DeviceDataParcelable(DeviceData data) {
        super(data.getDeviceName(), data.getProtocolInfo(), data.getDeviceLayout(), data.getDeviceModel());
    }

    public static final Creator<DeviceDataParcelable> CREATOR = new Creator<DeviceDataParcelable>() {
        @Override
        public DeviceDataParcelable createFromParcel(Parcel in) {
            return new DeviceDataParcelable(
                    in.readString(),
                    in.readInt(),
                    in.readInt(),
                    in.readInt()
            );
        }

        @Override
        public DeviceDataParcelable[] newArray(int size) {
            return new DeviceDataParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getDeviceName());
        dest.writeInt(getProtocolInfo());
        dest.writeInt(getDeviceLayout());
        dest.writeInt(getDeviceModel());
    }
}
