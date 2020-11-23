/*

        Copyright (C) 2020  Contributors (in contributors file)

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.remote.universalirremote;


import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.CopyOnWriteArrayList;

/*
        Class to discover and resolve services of same type in the local area network.
        Uses android.os.nsd.NsdManager for implementation.
 */
public class NetworkManager {

    // Context of the activity
    private Context _context;

    // Nsd API
    private NsdManager _nsdManager;

    private NsdManager.ResolveListener _resolveListener;
    private NsdManager.DiscoveryListener _discoveryListener;

    public static final String SERVICE_TYPE = "_http._tcp.";
    public static final String TAG = "NetworkManager";

    // Keys for Bundle passed in _discoveryHandler
    public static final String DISCOVERED_SERVICE_NAME = "disc.serv.name";
    public static final String DISCOVERED_SERVICE_TYPE = "disc.serv.type";

    // Key and result of Bundle passed in _discoveryHandler to identify if service lost or found.
    public static final String DISCOVER_OP = "disc.op";
    public static final int DISCOVER_NEW = 1;
    public static final int DISCOVER_LOST = 2;

    // Resolved service information.
    private NsdServiceInfo _selectedServiceInfo;

    // Handler discovery / loss of service on the network
    protected Handler _discoveryHandler;

    // List of all discovered services
    private CopyOnWriteArrayList<NsdServiceInfo> _discoveredServices;

    public NetworkManager(Context context, Handler discovery) {
        _context = context;
        _nsdManager = (NsdManager) _context.getSystemService(Context.NSD_SERVICE);
        _selectedServiceInfo = null;
        _discoveryHandler = discovery;
        _discoveredServices = new CopyOnWriteArrayList<NsdServiceInfo>();
    }

    public CopyOnWriteArrayList<NsdServiceInfo> getDiscoveredServices() {
        return _discoveredServices;
    }

    public NsdServiceInfo getDiscoveredService(String name, String serviceType ) {

        for (NsdServiceInfo i : _discoveredServices ) {
            if ((i.getServiceType() == serviceType) && (i.getServiceName() == name)) {
                return i;
            }
        }
        return null;
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return _selectedServiceInfo;
    }

    private void initialiseDiscoveryListener() {
        _discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery Start Failed " + errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery Stop Failed " + errorCode);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.e(TAG, "Discovery Started ");
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.e(TAG, "Discovery Stopped ");
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.i(TAG, serviceInfo.getServiceName()
                        + " " + serviceInfo.getServiceType() + " found");
                _discoveredServices.add(serviceInfo);
                if ((_discoveryHandler != null) && !_discoveredServices.contains(serviceInfo)) {
                    Bundle msgBundle = new Bundle();
                    msgBundle.putInt(DISCOVER_OP, DISCOVER_NEW);
                    msgBundle.putString(DISCOVERED_SERVICE_NAME, serviceInfo.getServiceName());
                    msgBundle.putString(DISCOVERED_SERVICE_TYPE, serviceInfo.getServiceType());
                    Message msg = new Message();
                    msg.setData(msgBundle);
                    _discoveryHandler.sendMessage(msg);

                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.i(TAG, serviceInfo.getServiceName()
                        + " " + serviceInfo.getServiceType() + " lost");
                _discoveredServices.remove(serviceInfo);
                if ((_discoveryHandler != null) && _discoveredServices.contains(serviceInfo)) {
                    Bundle msgBundle = new Bundle();
                    msgBundle.putInt(DISCOVER_OP, DISCOVER_LOST);
                    msgBundle.putString(DISCOVERED_SERVICE_NAME, serviceInfo.getServiceName());
                    msgBundle.putString(DISCOVERED_SERVICE_TYPE, serviceInfo.getServiceType());
                    Message msg = new Message();
                    msg.setData(msgBundle);
                    _discoveryHandler.sendMessage(msg);
                }
            }
        };
    }

}
