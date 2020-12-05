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

import java.util.List;

public interface DeviceButtonConfigCallback {

    public void allRawDataCallback( List<DeviceButtonConfig> allRawData );

    public void allRawDataForDeviceCallback( List<DeviceButtonConfig> allDeviceRawData );

    public void irTimingDataCallback(String irTiming);

    public void deviceButtonNameCallback(String buttonName);

}
