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

public class ApplicationWideSingleton {

    private static NsdServiceInfo _selectedService;
    private static String _selectedDevice;

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
        if(!isSelectedServiceValid()) {
            _selectedService = service;
            return true;
        }

        if(!_selectedService.equals(service)) {
            setSelectedService(service);
        }
        return false;
    }

    public static synchronized void setSelectedDevice(String device) {
        _selectedDevice = device;
    }

    public static synchronized String getSelectedDevice() {
        return _selectedDevice;
    }

    public static synchronized boolean isSelectedDeviceValid() {
        return (_selectedDevice != null);
    }

    public static synchronized boolean refreshSelectedDevice(String device) {
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
