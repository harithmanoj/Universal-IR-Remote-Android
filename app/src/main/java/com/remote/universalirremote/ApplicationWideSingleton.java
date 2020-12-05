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

package com.remote.universalirremote;

import android.net.nsd.NsdServiceInfo;

import com.remote.universalirremote.database.DeviceData;

public class ApplicationWideSingleton {

    private static NsdServiceInfo _selectedService;
    private static DeviceData _selectedDevice;

    public static synchronized void setSelectedService(NsdServiceInfo service) {
        _selectedService = service;
    }

    public static synchronized NsdServiceInfo getSelectedService() {
        return _selectedService;
    }

    public static synchronized boolean isSelectedServiceValid() {
        return (_selectedService != null);
    }

    public static synchronized boolean refreshSelectedService(NsdServiceInfo service) {
        if(service == null) {
            return false;
        }

        if(!isSelectedServiceValid()) {
            _selectedService = service;
            return true;
        }

        if(!_selectedService.equals(service)) {
            setSelectedService(service);
        }
        return false;
    }

    public static synchronized void setSelectedDevice(DeviceData device) {
        _selectedDevice = device;
    }

    public static synchronized DeviceData getSelectedDevice() {
        return _selectedDevice;
    }

    public static synchronized String getSelectedDeviceName() {
        return _selectedDevice.getDeviceName();
    }

    public static synchronized boolean isSelectedDeviceValid() {
        return (_selectedDevice != null);
    }

    public static synchronized boolean refreshSelectedDevice(DeviceData device) {
        if(device == null) {
            return false;
        }

        if(!isSelectedDeviceValid()) {
            _selectedDevice = device;
            return true;
        }

        if(!_selectedDevice.equals(device)) {
            setSelectedDevice(device);
        }
        return false;
    }
}
